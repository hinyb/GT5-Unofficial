package gregtech.api.ec;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISaveableComponent extends IComponent {
    default void saveNBTData(@NotNull NBTTagCompound nbt) {
        NBTTagCompound tag = saveComponentData();
        if (tag != null && !tag.hasNoTags()) {
            nbt.setTag(getComponentName(), tag);
        }
    }

    @Nullable NBTTagCompound saveComponentData();

    default void loadNBTData(@NotNull NBTTagCompound nbt) {
        if (nbt.hasKey(getComponentName())) {
            NBTTagCompound tag = nbt.getCompoundTag(getComponentName());
            loadComponentData(tag);
        }
    }

    void loadComponentData(@NotNull NBTTagCompound nbt);
}
