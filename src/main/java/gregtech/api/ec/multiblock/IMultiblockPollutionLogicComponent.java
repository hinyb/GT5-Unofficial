package gregtech.api.ec.multiblock;

import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMuffler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMultiblockPollutionLogicComponent extends IPollutionLogicComponent, IHatchContainerComponent {
    @NotNull List<MTEHatchMuffler> getValidHatches();
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
}
