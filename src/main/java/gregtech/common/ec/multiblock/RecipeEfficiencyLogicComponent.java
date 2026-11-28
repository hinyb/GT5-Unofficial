package gregtech.common.ec.multiblock;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.ec.IECMachine.SCANNER_INFO_DATA_PROVIDER;
import static gregtech.api.ec.IECMachine.SCANNER_INFO_MAP_PROVIDER;
import static gregtech.api.ec.IECMachine.WAILA_DATA_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_SYNCABLE;
import static gregtech.api.ec.IECMultiblock.ON_MACHINE_STOP;
import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_FINISH;
import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_IDLE;
import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_START;
import static gregtech.api.interfaces.tileentity.IGregTechDeviceInformation.encode;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.OrderedSectionBuilder;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.ISavableComponent;
import gregtech.api.ec.component.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.component.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.api.ec.gui.MultiblockGuiBuilder;
import gregtech.api.ec.signal.IGuiProvider;
import gregtech.api.ec.signal.IScannerInfoDataProvider;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class RecipeEfficiencyLogicComponent extends BaseMultiblockComponent
    implements IRecipeEfficiencyLogicComponent, ISavableComponent {

    private boolean instantMaxEfficiencyOnStart = true;
    private @NotNull ToIntFunction<IRecipeLogicComponent> increaseCalculator = _ -> 10000;

    public RecipeEfficiencyLogicComponent setRampUpEfficiencyPerRecipe(int efficiencyIncreate) {
        this.instantMaxEfficiencyOnStart = false;
        this.increaseCalculator = _ -> efficiencyIncreate;
        return this;
    }

    public RecipeEfficiencyLogicComponent setRampUpEfficiencyPerRecipe(
        @NotNull ToIntFunction<IRecipeLogicComponent> increaseCalculator) {
        this.instantMaxEfficiencyOnStart = false;
        this.increaseCalculator = increaseCalculator;
        return this;
    }

    public void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        mEfficiency = Math.clamp(mEfficiency + mEfficiencyIncrease, 0, getEffectiveMaxEfficiency());
        mEfficiencyIncrease = 0;
    }

    public void onRecipeIdle(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (mEfficiency > 0) {
            mEfficiency = Math.max(0, mEfficiency - mEfficiencyDecrease);
        }
    }

    public void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (instantMaxEfficiencyOnStart) {
            mEfficiency = getEffectiveMaxEfficiency();
            mEfficiencyIncrease = getBaseMaxEfficiency();
        } else {
            mEfficiencyIncrease = increaseCalculator.applyAsInt(recipeLogicComponent);
        }
    }

    public void onMachineStop(@NotNull ShutDownReason reason) {
        mEfficiency = 0;
        mEfficiencyIncrease = 0;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_MACHINE_STOP, this::onMachineStop);
        connect(ON_RECIPE_FINISH, IRecipeLogicComponent.IOnRecipeFinish.EFFICIENCY, this::onRecipeFinish);
        connect(ON_RECIPE_IDLE, IRecipeLogicComponent.IOnRecipeIdle.EFFICIENCY, this::onRecipeIdle);
        connect(ON_RECIPE_START, IRecipeLogicComponent.IOnRecipeStart.EFFICIENCY, this::onRecipeStart);
        connect(SCANNER_INFO_DATA_PROVIDER, this::appendInfoData);
        connect(SCANNER_INFO_MAP_PROVIDER, this::appendInfoMap);
        connect(WAILA_DATA_PROVIDER, this::appendWailaNBTData);
        connect(GUI_PROVIDER, this::configureGui);
        connect(GUI_SYNCABLE, this::registerGuiSyncValues);
    }

    public void appendInfoData(@NotNull OrderedSectionBuilder<Consumer<List<String>>> builder) {
        builder.addIf(
            mEfficiency > 0,
            IScannerInfoDataProvider.Section.EFFICIENCY,
            data -> { data.add(encode("GT5U.multiblock.scanner.efficiency", formatNumber(mEfficiency / 100.0F))); });
    }

    public void appendInfoMap(@NotNull Map<String, String> map) {
        map.put("energyEfficiency", Double.toString(mEfficiency / 10_000F));
    }

    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        tag.setFloat("efficiency", mEfficiency / 100.0F);
    }

    public void registerGuiSyncValues(@NotNull PanelSyncManager syncManager) {
        if (showInTerminal) {
            syncManager.syncValue("efficiency", new IntSyncValue(this::getEfficiency));
        }
    }

    public void configureGui(@NotNull MultiblockGuiBuilder builder) {
        builder.terminalTexts()
            .addIf(showInTerminal, IGuiProvider.TerminalSection.EFFICIENCY, (panel, syncManager) -> {
                IntSyncValue efficiency = syncManager.findSyncHandler("efficiency", IntSyncValue.class);
                return IKey.dynamic(
                    () -> EnumChatFormatting.WHITE + StatCollector.translateToLocal("GT5U.multiblock.efficiency")
                        + ": "
                        + formatNumber(efficiency.getValue() / 100D)
                        + "%"
                // enabled if active
                )
                    .asWidget()
                    .setEnabledIf(
                        (w) -> getMachine().getBaseMetaTileEntity()
                            .isActive());
            });
    }

    @FunctionalInterface
    public interface IMaxEfficiencyCalculator {

        @Range(from = 0, to = 10000)
        int getMaxEfficiency(@Nullable ItemStack aStack);
    }

    private IMaxEfficiencyCalculator calculator = stack -> 10000;
    private int mEfficiency = 0;
    private int mEfficiencyIncrease = 0;
    private int mEfficiencyDecrease = 1000;
    private boolean showInTerminal = false;

    public RecipeEfficiencyLogicComponent setShowInTerminal(boolean showInTerminal) {
        this.showInTerminal = showInTerminal;
        return this;
    }

    @Override
    public int getEfficiency() {
        return mEfficiency;
    }

    @Override
    public RecipeEfficiencyLogicComponent setEfficiency(int efficiency) {
        mEfficiency = Math.max(0, efficiency);
        return this;
    }

    @Override
    public int getBaseMaxEfficiency() {
        return calculator.getMaxEfficiency(getMachine().getControllerSlot());
    }

    @Override
    public int getEffectiveMaxEfficiency() {
        int max = calculator.getMaxEfficiency(getMachine().getControllerSlot());
        if (maintenanceLogic != null) {
            max -= maintenanceLogic.getMaintenanceEfficiencyPenalty();
        }
        return Math.max(0, max);
    }

    public RecipeEfficiencyLogicComponent setMaxEfficiencyCalculator(IMaxEfficiencyCalculator calculator) {
        this.calculator = calculator;
        return this;
    }

    @Override
    public RecipeEfficiencyLogicComponent setEfficiencyDecrease(int mEfficiencyDecrease) {
        this.mEfficiencyDecrease = mEfficiencyDecrease;
        return this;
    }

    @Override
    public RecipeEfficiencyLogicComponent setEfficiencyIncrease(int efficiencyIncrease) {
        this.mEfficiencyIncrease = efficiencyIncrease;
        return this;
    }

    @Override
    public @NotNull String getSaveKey() {
        return "eff";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var aNBT = new NBTTagCompound();
        aNBT.setInteger("mEfficiencyIncrease", mEfficiencyIncrease);
        aNBT.setInteger("mEfficiency", mEfficiency);
        return aNBT;
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound aNBT) {
        loadLegacyNBTData(aNBT);
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound aNBT) {
        mEfficiencyIncrease = aNBT.getInteger("mEfficiencyIncrease");
        mEfficiency = aNBT.getInteger("mEfficiency");
    }

    private @Nullable IMaintenanceLogicComponent maintenanceLogic;

    @Override
    public void onComponentsReady() {
        maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.KEY);
    }
}
