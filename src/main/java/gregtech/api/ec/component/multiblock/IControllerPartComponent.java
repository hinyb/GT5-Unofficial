package gregtech.api.ec.component.multiblock;

import net.minecraft.item.ItemStack;

import gregtech.api.ec.ComponentKey;

public interface IControllerPartComponent extends IMultiblockComponent {

    ComponentKey.ListKey<IControllerPartComponent> KEY = ComponentKey.list();

    /**
     * Check the ItemStack in the controller slot.
     *
     * @param aStack the ItemStack in the controller slot.
     * @return {@code true} if the item is valid for this machine or the machine doesn't have restrictions on the
     *         controller slot.
     */
    boolean isCorrectMachinePart(ItemStack aStack);
}
