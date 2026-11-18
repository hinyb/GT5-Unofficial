package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.ec.IGuiSyncableComponent;
import gregtech.api.ec.IOnServerPostTickComponent;
import gregtech.api.ec.multiblock.IPostStructureCheckComponent;
import gregtech.api.ec.multiblock.IPreStructureCheckComponent;
import gregtech.api.ec.multiblock.IStructureCheckHandlerComponent;
import gregtech.api.ec.multiblock.IStructureLogicComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class StructureLogicComponent extends BaseMultiblockComponent
    implements IStructureLogicComponent, IOnServerPostTickComponent, IGuiSyncableComponent {

    private boolean isFormed = false;
    private int checkTimer = 0;
    private int startUpCheckTimer = 100;
    private boolean isStructureDirty = false;
    private boolean forceCheckNextTick = false;
    private final List<StructureError> structureErrors = new ArrayList<>();

    @Override
    public boolean isFormed() {
        return isFormed;
    }

    @Override
    public void setFormed(boolean isFormed) {
        this.isFormed = isFormed;
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
        for (var component : preStructureChecks) {
            component.onPreStructureCheck(te);
        }
        structureErrors.clear();
        for (var handler : structureCheckHandlers) {
            handler.checkMachine(te, structureErrors);
        }
        isFormed = structureErrors.isEmpty();
        for (var component : postStructureChecks) {
            component.onPostStructureCheck(te, structureErrors);
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

    @Override
    public void onServerPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (isStructureDirty) {
            if (checkTimer <= 0) {
                checkTimer = 50;
            }
            isStructureDirty = false;
        }
        boolean shouldCheckThisTick = false;
        if (forceCheckNextTick) {
            shouldCheckThisTick = true;
            forceCheckNextTick = false;
            checkTimer = 0;
        } else {
            // -20 is for the GUI structure button cooldown
            if (checkTimer > -20 && --checkTimer == 0) {
                shouldCheckThisTick = true;
            }
            if (startUpCheckTimer >= 0 && --startUpCheckTimer == 0) {
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
    public int getServerTickPriority() {
        return ServerTickPriority.STRUCTURE_CHECK.priority;
    }

    @Override
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

    private IPreStructureCheckComponent[] preStructureChecks = IPreStructureCheckComponent.KEY.emptyArray;
    private IStructureCheckHandlerComponent[] structureCheckHandlers = IStructureCheckHandlerComponent.KEY.emptyArray;
    private IPostStructureCheckComponent[] postStructureChecks = IPostStructureCheckComponent.KEY.emptyArray;

    @Override
    public void onComponentsReady() {
        preStructureChecks = getComponents(IPreStructureCheckComponent.KEY);
        structureCheckHandlers = getComponents(IStructureCheckHandlerComponent.KEY);
        postStructureChecks = getComponents(IPostStructureCheckComponent.KEY);
    }
}
