package gregtech.common.ec.multiblock;

import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IInputSeparationComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputSeparationComponent extends BaseMultiblockComponent implements IInputSeparationComponent, ISavableComponent {
    protected static final String INPUT_SEPARATION_NBT_KEY = "inputSeparation";
    private boolean inputSeparation = true;
    @Override
    public boolean isInputSeparationEnabled() {
        return inputSeparation;
    }

    @Override
    public IInputSeparationComponent setInputSeparation(boolean enabled) {
        inputSeparation = enabled;
        return this;
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean(INPUT_SEPARATION_NBT_KEY, inputSeparation);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        inputSeparation = nbt.getBoolean(INPUT_SEPARATION_NBT_KEY);
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        inputSeparation = nbt.getBoolean(INPUT_SEPARATION_NBT_KEY);
    }
}
