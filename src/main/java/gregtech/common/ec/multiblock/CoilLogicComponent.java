package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_ACTIVE_STATE_CHANGE;
import static gregtech.api.ec.IECMachine.ON_MACHINE_REMOVAL;
import static gregtech.api.ec.IECMachine.ON_MACHINE_UNLOAD;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.ON_STRUCTURE_VERIFY;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.POST_STRUCTURE_CHECK;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.PRE_STRUCTURE_CHECK;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.multiblock.ICoilLogicComponent;
import gregtech.api.ec.component.multiblock.IStructureLogicComponent;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.common.data.GTCoilTracker;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class CoilLogicComponent extends BaseMultiblockComponent implements ICoilLogicComponent {

    private final LongArrayList mCoils = new LongArrayList();
    private GTCoilTracker.MultiCoilLease coilLease = null;
    private @NotNull HeatingCoilLevel coilLevel = HeatingCoilLevel.None;
    private @Nullable Consumer<HeatingCoilLevel> onCoilValidated = null;

    public CoilLogicComponent setOnCoilValidated(@Nullable Consumer<HeatingCoilLevel> onCoilValidated) {
        this.onCoilValidated = onCoilValidated;
        return this;
    }

    @Override
    public void addCoilCoordinate(long packedCorrdinate) {
        mCoils.add(packedCorrdinate);
    }

    @Override
    public @NotNull LongArrayList getCoilCoordinates() {
        return mCoils;
    }

    @Override
    public @NotNull HeatingCoilLevel getCoilLevel() {
        return coilLevel;
    }

    @Override
    public void setCoilLevel(@NotNull HeatingCoilLevel level) {
        coilLevel = level;
    }

    protected void activateCoilLease() {
        if (!mCoils.isEmpty() && coilLease == null) {
            coilLease = GTCoilTracker.activate(getMachine(), mCoils);
        }
    }

    protected void deactivateCoilLease() {
        if (coilLease != null) {
            GTCoilTracker.deactivate(coilLease);
            coilLease = null;
        }
    }

    private void clearAndDeactivate() {
        mCoils.clear();
        coilLevel = HeatingCoilLevel.None;
        deactivateCoilLease();
    }

    public void onPostStructureCheck(@NotNull IGregTechTileEntity te, @NotNull List<StructureError> errors) {
        if (errors.isEmpty()) {
            if (te.isActive()) {
                activateCoilLease();
            }
            if (onCoilValidated != null) {
                onCoilValidated.accept(coilLevel);
            }
        }
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_ACTIVE_STATE_CHANGE, (igte, active) -> {
            if (active && isMachineFormed()) {
                activateCoilLease();
            } else if (!active) {
                deactivateCoilLease();
            }
        });
        connect(ON_MACHINE_REMOVAL, this::clearAndDeactivate);
        connect(ON_MACHINE_UNLOAD, this::clearAndDeactivate);
        connect(PRE_STRUCTURE_CHECK, (_) -> clearAndDeactivate());
        connect(POST_STRUCTURE_CHECK, IStructureLogicComponent.IPostStructureCheck.COIL, this::onPostStructureCheck);
        connect(ON_STRUCTURE_VERIFY, IStructureLogicComponent.IStructureCheckHandler.COIL, this::checkMachine);
    }

    public void checkMachine(@NotNull IGregTechTileEntity aBaseMetaTileEntity, @NotNull List<StructureError> errors) {
        if (coilLevel == HeatingCoilLevel.None) {
            errors.add(StructureErrorRegistry.COIL_LEVEL_NOT_ENOUGH);
        }
    }
}
