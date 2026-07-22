package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import org.jetbrains.annotations.NotNull;

public interface ISmartHatchLogicComponent extends IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
}
