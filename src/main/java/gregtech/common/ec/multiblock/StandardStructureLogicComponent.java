package gregtech.common.ec.multiblock;

import gregtech.api.ec.IServerTickableComponent;
import gregtech.api.ec.multiblock.IPostStructureCheckComponent;
import gregtech.api.ec.multiblock.IPreStructureCheckComponent;
import gregtech.api.ec.multiblock.IStructureCheckHandlerComponent;
import gregtech.api.ec.multiblock.IStructureLogicComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StandardStructureLogicComponent extends BaseMultiblockComponent implements IStructureLogicComponent, IServerTickableComponent {
    private boolean isFormed = false;
    private int checkTimer = 0;
    private int startUpCheckTimer = 100;
    private boolean checkPending = false;
    private final List<StructureError> structureErrors = new ArrayList<>();


    @Override
    public boolean isFormed() {
        return isFormed;
    }

    @Override
    public void scheduleStructureCheck() {
        checkPending = true;
    }

    @Override
    public boolean checkStructure(IGregTechTileEntity te) {
        for (var component : preStructureChecks) {
            component.onPreStructureCheck(te);
        }
        structureErrors.clear();
        for (var handler : structureCheckHandlers) {
            handler.tryCheckMachine(te, structureErrors);
        }
        for (var component : postStructureChecks) {
            component.onPostStructureCheck(te, structureErrors);
        }
        isFormed = structureErrors.isEmpty();
        return isFormed;
    }

    @Override
    public List<StructureError> getStructureErrors() {
        return structureErrors;
    }

    private List<IPreStructureCheckComponent> preStructureChecks;
    private List<IStructureCheckHandlerComponent> structureCheckHandlers;
    private List<IPostStructureCheckComponent> postStructureChecks;

    @Override
    public void onComponentsReady() {
        preStructureChecks = getComponents(IPreStructureCheckComponent.class);
        structureCheckHandlers = getComponents(IStructureCheckHandlerComponent.class);
        postStructureChecks = getComponents(IPostStructureCheckComponent.class);
    }

    @Override
    public void postServerTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (checkPending) {
            if (checkTimer <= 0) {
                checkTimer = 50;
            }
            checkPending = false;
        }
        if (--checkTimer == 0 || --startUpCheckTimer == 0) {
            checkStructure(aBaseMetaTileEntity);
        }
        if (startUpCheckTimer < 0) {
            if (!isFormed && aBaseMetaTileEntity.isAllowedToWork()) {
                stopMachine(ShutDownReasonRegistry.STRUCTURE_INCOMPLETE);
            }
        }
    }

    @Override
    public @NotNull ServerTickPriority getServerTickPriority() {
        return ServerTickPriority.STRUCTURE_CHECK;
    }
}
