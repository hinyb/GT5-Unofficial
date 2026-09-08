package gregtech.api.ec;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IItemOutputLogicComponent extends IUniqueComponent {
    boolean addOutputAtomic(@NotNull ItemStack stack);
    boolean addOutputAtomic(@NotNull List<ItemStack> stacks);

    int addOutputPartial(@NotNull ItemStack stack, boolean protectExcess);
    default boolean addOutputPartial(@NotNull List<ItemStack> stacks, boolean protectExcess) {
        return addOutputPartial(stacks, protectExcess, null);
    }
    boolean addOutputPartial(@NotNull List<ItemStack> stacks, boolean protectExcess, @Nullable List<ItemStack> remaining);
}
