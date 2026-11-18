package gregtech.api.ec;

import static gregtech.GTLoggers.GT_FML_LOGGER;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISavableComponent extends IComponent {

    ComponentKey.ListKey<ISavableComponent> KEY = ComponentKey.list();

    @NotNull
    String getSaveKey();

    default void saveNBTData(@NotNull NBTTagCompound nbt) {
        var key = getSaveKey();
        if (nbt.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
            GT_FML_LOGGER.error(
                "Duplicate NBT save key '{}' in component {} on machine {}",
                key,
                getClass().getName(),
                getMachine().getClass()
                    .getName());
        }
        NBTTagCompound tag = saveComponentData();
        if (tag != null && !tag.hasNoTags()) {
            nbt.setTag(key, tag);
        }
    }

    @Nullable
    NBTTagCompound saveComponentData();

    default void loadNBTData(@NotNull NBTTagCompound nbt) {
        var key = getSaveKey();
        if (!nbt.hasKey(key, Constants.NBT.TAG_COMPOUND)) {
            loadLegacyNBTData(nbt);
        } else {
            NBTTagCompound tag = nbt.getCompoundTag(key);
            loadComponentData(tag);
        }
    }

    default void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {};

    void loadComponentData(@NotNull NBTTagCompound nbt);
}
