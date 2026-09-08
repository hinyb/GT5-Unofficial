package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnMachineRemovalComponent;
import gregtech.api.ec.IOnMachineUnloadComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IServerTickableComponent;
import gregtech.api.ec.multiblock.ICoilLogicComponent;
import gregtech.api.ec.multiblock.IPreStructureCheckComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.data.GTCoilTracker;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoilLogicComponent extends BaseMultiblockComponent implements ICoilLogicComponent, IPreStructureCheckComponent, IOnMachineUnloadComponent, IOnMachineRemovalComponent, IServerTickableComponent {
    private LongArrayList mCoils = new LongArrayList();
    private GTCoilTracker.MultiCoilLease coilLease = null;

    @Override
    public void addCoilCoordinate(long packedCorrdinate) {
        mCoils.add(packedCorrdinate);
    }

    @Override
    public @NotNull LongArrayList getCoilCoordinates() {
        return mCoils;
    }

    protected void deactivateCoilLease() {
        if (coilLease != null) {
            GTCoilTracker.deactivate(coilLease);
            coilLease = null;
        }
    }

    private void clearAndDeactivate() {
        mCoils.clear();
        deactivateCoilLease();
    }

    @Override
    public void onMachineUnload() {
        clearAndDeactivate();
    }

    @Override
    public void onMachineRemoval() {
        clearAndDeactivate();
    }

    @Override
    public void onPreStructureCheck(IGregTechTileEntity te) {
        clearAndDeactivate();
    }

    @Override
    public void postServerTick(IGregTechTileEntity te, long aTick) {
        boolean isProcessing = recipeLogic != null && recipeLogic.getMaxProgressTime() > 0;
        boolean isFormed = isMachineFormed();
        if (!isFormed || !isProcessing) {
            deactivateCoilLease();
            return;
        }
        if (!mCoils.isEmpty() && coilLease == null) {
            // todo refactor
            coilLease = GTCoilTracker.activate((MTEMultiBlockBase) getMachine(), mCoils);
        }
    }

    @Override
    public @NotNull ServerTickPriority getServerTickPriority() {
        return ServerTickPriority.COIL;
    }


    private @Nullable IRecipeLogicComponent recipeLogic;
    @Override
    public void onComponentsReady() {
        recipeLogic = tryGetComponent(IRecipeLogicComponent.class);
    }
}
