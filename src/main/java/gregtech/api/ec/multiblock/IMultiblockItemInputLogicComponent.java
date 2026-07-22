package gregtech.api.ec.multiblock;

import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMultiblockItemInputLogicComponent extends IItemInputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchInputBus> getValidHatches();
}
