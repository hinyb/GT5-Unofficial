package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.common.tileentities.machines.multi.drone.MTEHatchDroneDownLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMaintenanceLogicComponent extends IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachHatch(part);
    }
    boolean tryAttachHatch(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchMaintenance> getValidHatches();
    @Nullable MTEHatchDroneDownLink tryGetDroneDownLink();
}
