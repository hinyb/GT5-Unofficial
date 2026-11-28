package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_MACHINE_REMOVAL;
import static gregtech.api.ec.IECMachine.ON_MACHINE_UNLOAD;
import static gregtech.api.ec.IECMachine.ON_WORK_STATE_CHANGE;
import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_FINISH;
import static gregtech.api.ec.component.multiblock.IHatchContainerComponent.ON_HATCH_ATTACHED;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.PRE_STRUCTURE_CHECK;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.multiblock.IMaintenanceLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.multi.drone.MTEDroneCentre;
import gregtech.common.tileentities.machines.multi.drone.MTEHatchDroneDownLink;
import gregtech.common.tileentities.machines.multi.drone.production.ProductionRecord;

public class DroneDownLinkLogicComponent extends BaseMultiblockComponent {

    private final List<MTEHatchDroneDownLink> downLinks = new ArrayList<>();

    public void onHatchAttached(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEHatchDroneDownLink ddl) {
            if (!downLinks.contains(ddl)) {
                ddl.registerMachineController(getMachine());
                downLinks.add(ddl);
            }
        }
    }

    public void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        for (var ddl : validMTEList(downLinks)) {
            MTEDroneCentre centre = ddl.getCentre();
            if (centre == null) continue;
            ProductionRecord pdr = centre.productionDataRecorder;
            if (pdr.isActive()) pdr.addRecord(
                ((long) recipeLogicComponent.getMaxProgressTime()) * recipeLogicComponent.getEUt(),
                recipeLogicComponent.getOutputItems(),
                recipeLogicComponent.getOutputFluids());
        }

    }

    public void onWorkStateChanged(boolean enabled) {
        if (enabled) {
            for (var ddl : validMTEList(downLinks)) {
                ddl.findConnection(getMachine())
                    .ifPresent(conn -> conn.setActive(true));
            }
        } else {
            for (var ddl : validMTEList(downLinks)) {
                ddl.findConnection(getMachine())
                    .ifPresent(conn -> {
                        conn.setActive(false);
                        conn.setShutdownReason(
                            getMachine().getBaseMetaTileEntity()
                                .getLastShutDownReason());
                    });
            }
        }
    }

    @Override
    public void onComponentsReady() {
        getComponent(IMaintenanceLogicComponent.KEY);
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_HATCH_ATTACHED, this::onHatchAttached);
        connect(ON_MACHINE_REMOVAL, downLinks::clear);
        connect(ON_MACHINE_UNLOAD, downLinks::clear);
        connect(PRE_STRUCTURE_CHECK, (_) -> downLinks.clear());
        connect(ON_WORK_STATE_CHANGE, this::onWorkStateChanged);
        connect(ON_RECIPE_FINISH, IRecipeLogicComponent.IOnRecipeFinish.DRONE_COLLECT_DATA, this::onRecipeFinish);
    }
}
