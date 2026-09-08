package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.tileentities.machines.IDualInputHatch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDualInputLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachDualInput(part);
    }
    boolean tryAttachDualInput(@NotNull IMetaTileEntity part);
    @NotNull List<IDualInputHatch> getValidHatches();
}
