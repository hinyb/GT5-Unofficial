package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IBeamlineInputLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachBeamlineInput(part);
    }
    boolean tryAttachBeamlineInput(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchInputBeamline> getValidHatches();
}
