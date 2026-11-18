package gregtech.api.ec;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItemOutputLogicComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IItemOutputLogicComponent> KEY = ComponentKey.unique();

    boolean addOutputAtomic(@NotNull ItemStack stack);

    boolean addOutputAtomic(@NotNull List<ItemStack> stacks);

    int addOutputPartial(@NotNull ItemStack stack, boolean protectExcess);

    default boolean addOutputPartial(@NotNull List<ItemStack> stacks, boolean protectExcess) {
        return addOutputPartial(stacks, protectExcess, null);
    }

    boolean addOutputPartial(@NotNull List<ItemStack> stacks, boolean protectExcess,
        @Nullable List<ItemStack> remaining);
}
