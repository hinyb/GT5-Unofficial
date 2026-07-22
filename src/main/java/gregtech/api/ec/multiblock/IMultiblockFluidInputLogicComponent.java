package gregtech.api.ec.multiblock;

import gregtech.api.ec.IFluidInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMultiblockFluidInputLogicComponent extends IFluidInputLogicComponent, IHatchContainerComponent {
    @NotNull List<MTEHatchInput> getValidHatches();
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
}
