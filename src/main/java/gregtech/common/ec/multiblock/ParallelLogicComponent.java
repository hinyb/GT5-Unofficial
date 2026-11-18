package gregtech.common.ec.multiblock;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IWailaDataComponent;
import gregtech.api.ec.multiblock.IParallelLogicComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class ParallelLogicComponent extends BaseMultiblockComponent
    implements IParallelLogicComponent, IProcessingLogicSetupComponent, ISavableComponent, IWailaDataComponent {

    private boolean alwaysMaxParallel = true;
    private int powerPanelMaxParallel = 1;
    private int maxParallelForPanel = 1;

    public ParallelLogicComponent(@NotNull IMaxParallelProvider maxParallelProvider) {
        this.maxParallelProvider = maxParallelProvider;
    }

    @Override
    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        final int localMaxParallel = getMaxParallelRecipes();
        final int localPowerPanelParallel = getTrueParallel();

        tag.setInteger("maxParallelRecipes", localMaxParallel);
        if (localPowerPanelParallel != localMaxParallel) {
            tag.setInteger("powerPanelMaxParallel", localPowerPanelParallel);
        }
    }

    @Override
    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setMaxParallelSupplier(this::getTrueParallel);
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
