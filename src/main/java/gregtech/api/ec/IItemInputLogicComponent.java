package gregtech.api.ec;

import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IItemInputLogicComponent extends IComponent {
    default boolean depleteInputAtomic(@NotNull ItemStack aStack) {
        return depleteInputAtomic(aStack, false);
    }

    boolean depleteInputAtomic(@NotNull ItemStack aStack, boolean simulate);

    default void appendStoredItems(@NotNull ArrayList<ItemStack> list) {
        appendStoredItemsForColor(list, null);
    }

    void appendStoredItemsForColor(@NotNull ArrayList<ItemStack> list, @Nullable Byte color);

    void appendStoredItemsFromME(@NotNull Map<GTUtility.ItemId, ItemStack> map);
}
