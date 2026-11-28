package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.WAILA_DATA_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_PROVIDER;
import static gregtech.api.ec.component.IProcessingLogicRecipeComponent.PROCESSING_LOGIC_SETUP;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.ISavableComponent;
import gregtech.api.ec.component.multiblock.IParallelLogicComponent;
import gregtech.api.ec.gui.MultiblockGuiBuilder;
import gregtech.api.ec.signal.IGuiProvider;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class ParallelLogicComponent extends BaseMultiblockComponent
    implements IParallelLogicComponent, ISavableComponent {

    private boolean alwaysMaxParallel = true;
    private int powerPanelMaxParallel = 1;
    private int maxParallelForPanel = 1;

    public ParallelLogicComponent(@NotNull IMaxParallelProvider maxParallelProvider) {
        this.maxParallelProvider = maxParallelProvider;
    }

    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        final int localMaxParallel = getMaxParallelRecipes();
        final int localPowerPanelParallel = getTrueParallel();

        tag.setInteger("maxParallelRecipes", localMaxParallel);
        if (localPowerPanelParallel != localMaxParallel) {
            tag.setInteger("powerPanelMaxParallel", localPowerPanelParallel);
        }
    }

    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setMaxParallelSupplier(this::getTrueParallel);
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(PROCESSING_LOGIC_SETUP, this::setupProcessingLogic);
        connect(WAILA_DATA_PROVIDER, this::appendWailaNBTData);
        connect(GUI_PROVIDER, this::configureGui);
    }

    public void configureGui(@NotNull MultiblockGuiBuilder builder) {
        builder.powerPanel()
            .add(
                IGuiProvider.PowerPanelSection.PARALLEL_LABEL,
                (panel, syncManager) -> IKey.lang("GTPP.CC.parallel")
                    .asWidget()
                    .marginBottom(4))
            .add(IGuiProvider.PowerPanelSection.PARALLEL_CONFIGURE, (panel, syncManager) -> {
                IntSyncValue maxParallelSyncer = new IntSyncValue(
                    this::getMaxParallelForPanel,
                    this::setMaxParallelForPanel,
                    () -> Math.max(this.getMaxParallelRecipes(), 1),
                    null);
                BooleanSyncValue alwaysMaxParallelSyncer = new BooleanSyncValue(
                    this::isAlwaysMaxParallel,
                    this::setAlwaysMaxParallel).allowC2S();
                syncManager.syncValue("maxParallel", maxParallelSyncer);
                syncManager.syncValue("alwaysMaxParallel", alwaysMaxParallelSyncer);

                IntSyncValue powerPanelMaxParallelSyncer = new IntSyncValue(
                    this::getPowerPanelMaxParallel,
                    this::setPowerPanelMaxParallel,
                    this::getTrueParallel,
                    (val) -> this
                        .setPowerPanelMaxParallel(Math.clamp(val, 1, Math.max(1, this.getMaxParallelRecipes()))))
                            .allowC2S();
                return Flow.row()
                    .fullWidth()
                    .marginBottom(4)
                    .height(18)
                    .paddingLeft(3)
                    .paddingRight(3)
                    .mainAxisAlignment(Alignment.MainAxis.CENTER)
                    .child(
                        makeParallelConfiguratorTextFieldWidget(
                            maxParallelSyncer,
                            alwaysMaxParallelSyncer,
                            powerPanelMaxParallelSyncer))
                    .child(new ButtonWidget<>().overlay(new DynamicDrawable(() -> {
                        if (alwaysMaxParallelSyncer.getValue()) {
                            return GTGuiTextures.OVERLAY_BUTTON_CHECKMARK;
                        } else {
                            return GTGuiTextures.OVERLAY_BUTTON_CROSS;
                        }
                    }))
                        .onMousePressed(d -> {
                            alwaysMaxParallelSyncer.setValue(!alwaysMaxParallelSyncer.getValue());
                            powerPanelMaxParallelSyncer.setValue(maxParallelSyncer.getValue());
                            return true;
                        })
                        .tooltipBuilder(t -> t.addLine(IKey.lang("GT5U.gui.button.max_parallel")))
                        .tooltipShowUpTimer(TOOLTIP_DELAY));
            });
    }

    protected IWidget makeParallelConfiguratorTextFieldWidget(IntSyncValue maxParallelSyncer,
        BooleanSyncValue alwaysMaxParallelSyncer, IntSyncValue powerPanelMaxParallelSyncer) {
        return new TextFieldWidget().value(powerPanelMaxParallelSyncer)
            .setTextAlignment(Alignment.Center)
            .numbersInt(
                () -> alwaysMaxParallelSyncer.getValue() ? maxParallelSyncer.getValue() : 1,
                maxParallelSyncer::getValue)
            .tooltipBuilder(
                t -> t.addLine(
                    IKey.dynamic(
                        () -> alwaysMaxParallelSyncer.getValue()
                            ? StatCollector
                                .translateToLocalFormatted("GT5U.gui.text.lockedvalue", maxParallelSyncer.getValue())
                            : StatCollector.translateToLocalFormatted(
                                "GT5U.gui.text.rangedvalue",
                                1,
                                maxParallelSyncer.getValue()))))
            .tooltipShowUpTimer(TOOLTIP_DELAY)
            .size(70, 14)
            .marginBottom(4)
            .marginRight(16);
    }

    @FunctionalInterface
    public interface IMaxParallelProvider {

        int getMaxParallelRecipes();
    }

    @NotNull
    private final IMaxParallelProvider maxParallelProvider;

    @Override
    public boolean isAlwaysMaxParallel() {
        return alwaysMaxParallel;
    }

    @Override
    public void setAlwaysMaxParallel(boolean alwaysMaxParallel) {
        this.alwaysMaxParallel = alwaysMaxParallel;
        var igte = getMachine().getBaseMetaTileEntity();
        if (igte.isServerSide()) {
            this.powerPanelMaxParallel = Math.max(1, getMaxParallelRecipes());
        }
    }

    @Override
    public int getPowerPanelMaxParallel() {
        return powerPanelMaxParallel;
    }

    @Override
    public void setPowerPanelMaxParallel(int maxParallel) {
        powerPanelMaxParallel = maxParallel;
    }

    @Override
    public int getMaxParallelRecipes() {
        return maxParallelProvider.getMaxParallelRecipes();
    }

    @Override
    public int getMaxParallelForPanel() {
        return maxParallelForPanel;
    }

    @Override
    public void setMaxParallelForPanel(int maxParallel) {
        maxParallelForPanel = maxParallel;
    }

    @Override
    public @NotNull String getSaveKey() {
        return "parallel";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setBoolean("alwaysMaxParallel", alwaysMaxParallel);
        nbt.setInteger("powerPanelMaxParallel", powerPanelMaxParallel);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        alwaysMaxParallel = !nbt.hasKey("alwaysMaxParallel") || nbt.getBoolean("alwaysMaxParallel");
        powerPanelMaxParallel = nbt.getInteger("powerPanelMaxParallel");
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        loadLegacyNBTData(nbt);
    }
}
