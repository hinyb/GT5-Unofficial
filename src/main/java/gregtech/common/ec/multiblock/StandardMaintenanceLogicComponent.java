package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.multi.drone.MTEHatchDroneDownLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardMaintenanceLogicComponent implements IMaintenanceLogicComponent {
    private final List<MTEHatchMaintenance> mMaintenanceHatches = new ArrayList<>();
    private @Nullable MTEHatchDroneDownLink ddl = null;

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchMaintenance hatch) {
            if (hatch instanceof MTEHatchDroneDownLink droneDownLink) {
                // todo refactor
                droneDownLink.registerMachineController((MTEMultiBlockBase) getMachine());
                ddl = droneDownLink;
            }
            if (!mMaintenanceHatches.contains(hatch))
            {
                mMaintenanceHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches()
    {
        mMaintenanceHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    @Override
    public @NotNull List<MTEHatchMaintenance> getValidHatches() {
        return filterValidMTEs(mMaintenanceHatches);
    }

    @Override
    public @Nullable MTEHatchDroneDownLink tryGetDroneDownLink() {
        return ddl;
    }

    private @Nullable IECMultiblock multiblock = null;
    @Override
    public void onAddToMultiblock(@NotNull IECMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public @Nullable IECMultiblock getMachine() {
        return multiblock;
    }
}
