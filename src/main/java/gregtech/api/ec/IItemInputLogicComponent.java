package gregtech.api.ec;

import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IItemInputLogicComponent extends IUniqueComponent {
    default boolean depleteInputAtomic(@NotNull ItemStack aStack) {
        return depleteInputAtomic(aStack, false);
    }

    boolean depleteInputAtomic(@NotNull ItemStack aStack, boolean simulate);

    default void appendStoredItems(@NotNull List<ItemStack> list) {
        appendStoredItemsForColor(list, null);
    }

    void appendStoredItemsForColor(@NotNull List<ItemStack> list, @Nullable Byte color);

    default List<ItemStack> getStoredItemsForColor(@Nullable Byte color)
    {
        List<ItemStack> list = new ArrayList<>();
        appendStoredItemsForColor(list, color);
        return list;
    }
}
