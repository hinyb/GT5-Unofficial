package gregtech.common.ec.multiblock;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.interfaces.tileentity.IGregTechDeviceInformation.encode;

import java.util.List;
import java.util.Map;

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
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ListWidget;

import gregtech.api.ec.IOnMachineStopComponent;
import gregtech.api.ec.IOnRecipeFinishComponent;
import gregtech.api.ec.IOnRecipeIdleComponent;
import gregtech.api.ec.IOnRecipeStartComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IScannerInfoComponent;
import gregtech.api.ec.IWailaDataComponent;
import gregtech.api.ec.gui.ITerminalTextWidgetComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class RecipeEfficiencyLogicComponent extends BaseMultiblockComponent implements IRecipeEfficiencyLogicComponent,
    IOnRecipeStartComponent, IOnRecipeFinishComponent, IOnRecipeIdleComponent, ISavableComponent,
    IOnMachineStopComponent, IScannerInfoComponent, IWailaDataComponent, ITerminalTextWidgetComponent {

    private boolean instantMaxEfficiencyOnStart = true;
    private int defaultEfficiencyIncrease = 10000;

    public RecipeEfficiencyLogicComponent setRampUpEfficiencyPerRecipe(int efficiencyIncreate) {
        this.instantMaxEfficiencyOnStart = false;
        this.defaultEfficiencyIncrease = efficiencyIncreate;
        return this;
    }

    @Override
    public void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        mEfficiency = Math.clamp(mEfficiency + mEfficiencyIncrease, 0, getMaxEfficiency());
        mEfficiencyIncrease = 0;
    }

    @Override
    public int getRecipeFinishPriority() {
        return RecipeFinishPriority.EFFICIENCY.priority;
    }

    @Override
    public void onRecipeIdle(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (mEfficiency > 0) {
            mEfficiency = Math.max(0, mEfficiency - mEfficiencyDecrease);
        }
    }

    @Override
    public int getRecipeIdlePriority() {
        return RecipeIdlePriority.EFFICIENCY.priority;
    }

    @Override
    public void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (instantMaxEfficiencyOnStart) {
            mEfficiency = getMaxEfficiency();
            mEfficiencyIncrease = defaultEfficiencyIncrease;
        } else if (mEfficiencyIncrease == 0) {
            mEfficiencyIncrease = defaultEfficiencyIncrease;
        }
    }

    @Override
    public int getRecipeStartPriority() {
        return RecipeStartPriority.EFFICIENCY.priority;
    }

    @Override
    public void onMachineStop(@NotNull ShutDownReason reason) {
        mEfficiency = 0;
        mEfficiencyIncrease = 0;
    }

    @Override
    public void appendInfoData(@NotNull List<String> data) {
        if (mEfficiency > 0) {
            data.add(encode("GT5U.multiblock.scanner.efficiency", formatNumber(mEfficiency / 100.0F)));
        }
    }

    @Override
    public void appendInfoMap(@NotNull Map<String, String> map) {
        map.put("energyEfficiency", Double.toString(mEfficiency / 10_000F));
    }

    @Override
    public int getScannerInfoPriority() {
        return ScannerInfoPriority.EFFICIENCY.priority;
    }

    @Override
    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        tag.setFloat("efficiency", mEfficiency / 100.0F);
    }

    @Override
    public void registerGuiSyncValues(@NotNull PanelSyncManager syncManager) {
        if (showInTerminal) {
            syncManager.syncValue("efficiency", new IntSyncValue(this::getEfficiency));
        }
    }

    @Override
    public void addTerminalTextWidget(@NotNull ListWidget<IWidget, ?> widgetList, PanelSyncManager syncManager,
        ModularPanel parent) {
        if (showInTerminal) {
            IntSyncValue efficency = syncManager.findSyncHandler("efficiency", IntSyncValue.class);
            widgetList.child(
                IKey.dynamic(
                    () -> EnumChatFormatting.WHITE + StatCollector.translateToLocal("GT5U.multiblock.efficiency")
                        + ": "
                        + formatNumber(efficency.getValue() / 100D)
                        + "%"
                // enabled if active
                )
                    .asWidget()
                    .setEnabledIf(
                        (w) -> getMachine().getBaseMetaTileEntity()
                            .isActive()));
        }
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
        mEfficiency = efficiency;
        return this;
    }

    @Override
    public int getMaxEfficiency() {
        int max = calculator.getMaxEfficiency(getMachine().getControllerSlot());
        if (maintenanceLogic != null) {
            max -= maintenanceLogic.getMaintenanceEfficiencyPenalty();
        }
        return Math.clamp(max, 0, 10000);
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
        aNBT.setInteger("mEfficiencyDecrease", mEfficiencyDecrease);
        aNBT.setInteger("mEfficiency", mEfficiency);
        return aNBT;
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound aNBT) {
        mEfficiencyDecrease = aNBT.getInteger("mEfficiencyDecrease");
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
