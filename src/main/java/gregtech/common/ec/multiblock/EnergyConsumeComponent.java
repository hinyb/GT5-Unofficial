package gregtech.common.ec.multiblock;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IEnergyInputLogicComponent;
import gregtech.api.ec.IEnergyOutputLogicComponent;
import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IScannerInfoComponent;
import gregtech.api.ec.IWailaDataComponent;
import gregtech.api.ec.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

// may need a refactor
public class EnergyConsumeComponent extends BaseMultiblockComponent
    implements IOnRecipeTickComponent, IScannerInfoComponent, IWailaDataComponent {

    private final ToLongFunction<IRecipeLogicComponent> getActualEnergyUsage;

    public EnergyConsumeComponent(ToLongFunction<IRecipeLogicComponent> getActualEnergyUsage) {
        this.getActualEnergyUsage = getActualEnergyUsage;
    }

    public EnergyConsumeComponent() {
        this.getActualEnergyUsage = this::getActualEnergyUsage;
    }

    private int getEfficiency() {
        return recipeEfficiencyLogic != null ? recipeEfficiencyLogic.getEfficiency() : 10000;
    }

    private long getActualEnergyUsage(IRecipeLogicComponent recipeLogicComponent) {
        return GTUtility.fastDivMul(-recipeLogicComponent.getEUt(), Math.max(1000, getEfficiency()), 10000);
    }

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        long mEUt = recipeLogicComponent.getEUt();
        if (mEUt > 0) {
            if (energyOutputLogic == null) {
                throw new IllegalStateException("Recipe produces energy but no energy output component is attached.");
            }
            energyOutputLogic.addEnergyOutput(GTUtility.fastDivMul(mEUt, 10000, getEfficiency()));
            return true;
        }
        if (mEUt < 0) {
            if (energyInputLogic == null) {
                throw new IllegalStateException("Recipe consumes energy but no energy input component is attached.");
            }
            if (!energyInputLogic.drainEnergyInput(getActualEnergyUsage.applyAsLong(recipeLogicComponent))) {
                stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                return false;
            }
        }
        return true;
    }

    @Override
    public int getRecipeTickPriority() {
        return RecipeTickPriority.CONSUME_ENERGY.priority;
    }

    private @Nullable IEnergyInputLogicComponent energyInputLogic;
    private @Nullable IEnergyOutputLogicComponent energyOutputLogic;
    private @Nullable IRecipeEfficiencyLogicComponent recipeEfficiencyLogic;
    private @Nullable IRecipeLogicComponent recipeLogicComponent;

    @Override
    public void onComponentsReady() {
        energyInputLogic = tryGetComponent(IEnergyInputLogicComponent.KEY);
        energyOutputLogic = tryGetComponent(IEnergyOutputLogicComponent.KEY);
        recipeEfficiencyLogic = tryGetComponent(IRecipeEfficiencyLogicComponent.KEY);
        recipeLogicComponent = tryGetComponent(IRecipeLogicComponent.KEY);
    }

    public long getActualEnergyUsage() {
        return recipeLogicComponent != null ? getActualEnergyUsage.applyAsLong(recipeLogicComponent) : 0;
    }

    @Override
    public void appendInfoData(@NotNull List<String> data) {
        if (getActualEnergyUsage() > 0) {
            data.add(
                StatCollector
                    .translateToLocalFormatted("GT5U.multiblock.scanner.usage", formatNumber(getActualEnergyUsage())));
        }
    }

    @Override
    public void appendInfoMap(@NotNull Map<String, String> map) {
        map.put("energyUsage", Long.toString(getActualEnergyUsage()));
    }

    @Override
    public int getScannerInfoPriority() {
        return ScannerInfoPriority.ENERGY_CONSUME.priority;
    }

    @Override
    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (recipeLogicComponent == null) return;
        if (getMachine().isActive()) {
            long EUt = recipeLogicComponent.getEUt();
            if (EUt < 0) tag.setLong("energyUsage", getActualEnergyUsage());
            else {
                tag.setLong("energyUsage", -GTUtility.fastDivMul(EUt, 10000, getEfficiency()));
            }
        }
    }
}
