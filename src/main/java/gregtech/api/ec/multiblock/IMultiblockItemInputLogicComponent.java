package gregtech.api.ec.multiblock;

import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IMultiblockItemInputLogicComponent extends IItemInputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachItemInput(part);
    }
    boolean tryAttachItemInput(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchInputBus> getValidHatches();
    void appendStoredItemsFromME(@NotNull Map<GTUtility.ItemId, ItemStack> map);
    short getHatchColors();
}
