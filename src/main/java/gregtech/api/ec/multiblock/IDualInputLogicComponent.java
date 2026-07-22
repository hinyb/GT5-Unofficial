package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.tileentities.machines.IDualInputHatch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDualInputLogicComponent extends IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
    @NotNull List<IDualInputHatch> getValidHatches();
}
