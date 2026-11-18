package gregtech.api.ec.multiblock;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.ComponentKey;

public interface IControllerToolWearComponent extends IMultiblockComponent {

    ComponentKey.ListKey<IControllerToolWearComponent> KEY = ComponentKey.list();

    long calculateDamageModifier(long eut);

    long getExpectedDamage(@Nullable ItemStack tool, long eut);
}
