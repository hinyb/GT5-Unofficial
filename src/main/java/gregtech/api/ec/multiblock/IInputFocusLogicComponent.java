package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEBusInputFocus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IInputFocusLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachInputFocus(part);
    }
    boolean tryAttachInputFocus(@NotNull IMetaTileEntity part);
    @NotNull List<MTEBusInputFocus> getValidHatches();
}
