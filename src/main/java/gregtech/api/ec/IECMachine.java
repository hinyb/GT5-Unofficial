package gregtech.api.ec;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IECMachine {

    <T extends IComponent> @Nullable T tryGetComponent(@NotNull ComponentKey.UniqueKey<T> key);

    default <T extends IComponent> @NotNull T getComponent(@NotNull ComponentKey.UniqueKey<T> key) {
        T component = tryGetComponent(key);
        if (component == null) {
            throw new IllegalStateException("Missing component: " + key.interfaceType.getName());
        }
        return component;
    }

    <T extends IComponent> boolean hasComponent(@NotNull ComponentKey<T> key);

    <T extends IComponent> @NotNull T[] getComponents(@NotNull ComponentKey.ListKey<T> key);

    <T extends IComponent> IECMachine addComponent(@NotNull T component);

    <T extends IComponent> boolean removeComponent(@NotNull T component);

    default <T extends IUniqueComponent> boolean removeComponent(@NotNull ComponentKey.UniqueKey<T> key) {
        T comp = tryGetComponent(key);
        return comp != null && removeComponent(comp);
    }

    <T extends IComponent> boolean removeComponent(@NotNull ComponentKey.ListKey<T> key);

    void markDirty();

    int getRandomNumber(int aRange);

    void disableWorking();

    void enableWorking();

    void sendLoopStart(byte aIndex);

    IGregTechTileEntity getBaseMetaTileEntity();

    ItemStack getMachineCraftingIcon();
}
