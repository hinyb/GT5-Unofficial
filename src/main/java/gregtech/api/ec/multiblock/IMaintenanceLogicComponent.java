package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.common.tileentities.machines.multi.drone.MTEHatchDroneDownLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMaintenanceLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachMaintenance(part);
    }
    boolean tryAttachMaintenance(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatchMaintenance> getValidHatches();
    @Nullable MTEHatchDroneDownLink tryGetDroneDownLink();
    boolean hasIssue(int mask);
    void setIssue(int mask, boolean hasIssue);
    int getMaintenanceErrorBits();
    int getRepairStatus();
    int getIdealStatus();
    void fixAllIssues();
    void causeAllIssues();
    void causeRandomIssue();
    void checkMaintenance();
    int getMaintenanceEfficiencyPenalty();
}
