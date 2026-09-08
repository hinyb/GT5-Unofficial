package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEHatchOutputBeamline;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBeamlineOutputLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachBeamlineOutput(part);
    }
    boolean tryAttachBeamlineOutput(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchOutputBeamline> getValidHatches();
}
