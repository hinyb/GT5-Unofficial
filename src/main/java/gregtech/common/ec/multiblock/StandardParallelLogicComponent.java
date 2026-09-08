package gregtech.common.ec.multiblock;

import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IParallelLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StandardParallelLogicComponent extends BaseMultiblockComponent implements IParallelLogicComponent, ISavableComponent {
    private boolean alwaysMaxParallel = true;
    private int powerPanelMaxParallel = 1;

    public StandardParallelLogicComponent(@NotNull IMaxParallelProvider maxParallelProvider) {
        this.maxParallelProvider = maxParallelProvider;
    }

    @FunctionalInterface
    public interface IMaxParallelProvider {
        int getMaxParallelRecipes();
    }

    @NotNull private final IMaxParallelProvider maxParallelProvider;

    @Override
    public boolean isAlwaysMaxParallel() {
        return alwaysMaxParallel;
    }

    @Override
    public void setAlwaysMaxParallel(boolean alwaysMaxParallel) {
        this.alwaysMaxParallel = alwaysMaxParallel;
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
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setBoolean("alwaysMaxParallel", alwaysMaxParallel);
        nbt.setInteger("powerPanelMaxParallel", powerPanelMaxParallel);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        alwaysMaxParallel = !nbt.hasKey("alwaysMaxParallel") || nbt.getBoolean("alwaysMaxParallel");
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        alwaysMaxParallel = !nbt.hasKey("alwaysMaxParallel") || nbt.getBoolean("alwaysMaxParallel");
        powerPanelMaxParallel = nbt.getInteger("powerPanelMaxParallel");
    }
}
