package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import org.jetbrains.annotations.NotNull;

public interface ISmartHatchLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachSmartHatch(part);
    }
    boolean tryAttachSmartHatch(@NotNull IMetaTileEntity part);
    boolean needsPeriodicChecks();
}
