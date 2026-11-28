package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_SERVER_POST_TICK;
import static gregtech.api.ec.IECMultiblock.GUI_SYNCABLE;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.multiblock.IStructureLogicComponent;
import gregtech.api.ec.signal.IOnServerPostTick;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class StructureLogicComponent extends BaseMultiblockComponent implements IStructureLogicComponent {

    private boolean isFormed = false;
    private boolean lastFormed = isFormed;
    private int checkTimer = 0;
    private int startUpCheckTimer = 100;
    private volatile boolean isStructureDirty = false;
    private boolean forceCheckNextTick = false;
    private final List<StructureError> structureErrors = new ArrayList<>();

    @Override
    public boolean isFormed() {
        return isFormed;
    }

    @Override
    public void markStructureDirty() {
        isStructureDirty = true;
    }

    @Override
    public void scheduleCheckNextTick() {
        forceCheckNextTick = true;
    }

    @Override
    public boolean checkStructure(IGregTechTileEntity te) {
        if (!te.isServerSide()) return isFormed;
        for (var listener : listeners(PRE_STRUCTURE_CHECK)) {
            listener.onPreStructureCheck(te);
        }
        structureErrors.clear();
        for (var listener : listeners(ON_STRUCTURE_SCAN)) {
            listener.checkMachine(te, structureErrors);
        }
        if (structureErrors.isEmpty()) {
            for (var listener : listeners(ON_STRUCTURE_VERIFY)) {
                listener.checkMachine(te, structureErrors);
            }
        }
        isFormed = structureErrors.isEmpty();
        for (var listener : listeners(POST_STRUCTURE_CHECK)) {
            listener.onPostStructureCheck(te, structureErrors);
        }
        if (lastFormed != isFormed) {
            lastFormed = isFormed;
            var key = isFormed ? ON_STRUCTURE_FORMED : ON_STRUCTURE_BROKEN;
            for (var listener : listeners(key)) {
                listener.onStructureStateChanged(te);
            }
        }
        if (isFormed()) {
            te.issueTileUpdate();
        }
        return isFormed;
    }

    @Override
    public List<StructureError> getStructureErrors() {
        return structureErrors;
    }

    @Override
    public int getStructureUpdateTime() {
        return checkTimer;
    }

    @Override
    public void setStructureUpdateTime(int time) {
        checkTimer = time;
    }

    @Override
    public int getStartUpCheckTimer() {
        return startUpCheckTimer;
    }

    public void onServerPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (isStructureDirty) {
            if (checkTimer <= 0) {
                checkTimer = 50;
            }
            isStructureDirty = false;
        }
        boolean shouldCheckThisTick = startUpCheckTimer >= 0 && --startUpCheckTimer == 0;
        if (forceCheckNextTick) {
            shouldCheckThisTick = true;
            forceCheckNextTick = false;
            checkTimer = 0;
        } else {
            // -20 is for the GUI structure button cooldown
            if (checkTimer > -20 && --checkTimer == 0) {
                shouldCheckThisTick = true;
            }
        }
        if (shouldCheckThisTick) {
            checkStructure(aBaseMetaTileEntity);
        }
        if (startUpCheckTimer < 0) {
            // It seems intentional here, skips the structure check when disabled (to allow swapping blocks safely),
            // even mid-recipe.
            if (!isFormed && (aBaseMetaTileEntity
                .isAllowedToWork()/* || recipeLogic != null && recipeLogic.getMaxProgressTime() > 0 */)) {
                stopMachine(ShutDownReasonRegistry.STRUCTURE_INCOMPLETE);
            }
        }
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_SERVER_POST_TICK, IOnServerPostTick.STRUCTURE_CHECK, this::onServerPostTick);
        connect(GUI_SYNCABLE, this::registerGuiSyncValues);
    }

    public void registerGuiSyncValues(@NotNull PanelSyncManager syncManager) {
        syncManager.syncValue(
            "structureErrors",
            new GenericListSyncHandler<>(
                () -> structureErrors,
                null,
                StructureErrorRegistry::deserialize,
                StructureErrorRegistry::serialize,
                Object::equals,
                StructureError::copy));
        syncManager.syncValue("machine", new BooleanSyncValue(() -> isFormed, val -> isFormed = val));
        IntSyncValue startupCheckSyncer = new IntSyncValue(() -> startUpCheckTimer, val -> startUpCheckTimer = val);
        syncManager.syncValue("startupCheck", startupCheckSyncer);
    }
}
