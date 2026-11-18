package gregtech.api.ec.multiblock;

import java.util.Map;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.util.GTUtility;

public interface IMEItemInputLogicComponent extends IMultiblockItemInputLogicComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IMEItemInputLogicComponent> KEY = ComponentKey.unique();

    @NotNull
    Map<GTUtility.ItemId, ItemStack> getStoredItemsFromME();
}
