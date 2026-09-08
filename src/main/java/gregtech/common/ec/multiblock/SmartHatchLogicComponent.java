package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.ISmartHatchLogicComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.IHatchWatcher;
import gregtech.common.tileentities.machines.ISmartInputHatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmartHatchLogicComponent extends BaseMultiblockComponent implements ISmartHatchLogicComponent {
    private final List<ISmartInputHatch> mSmartInputHatches = new ArrayList<>();
    private boolean doPeriodicChecks = false;

    @Override
    public boolean tryAttachSmartHatch(@NotNull IMetaTileEntity part) {
        if (part instanceof ISmartInputHatch hatch) {
            mSmartInputHatches.add(hatch);
            hatch.addWatcher((IHatchWatcher) getMachine());
            doPeriodicChecks |= hatch.needsPeriodicChecks();
            return true;
        }
        return false;
    }

    @Override
    public boolean needsPeriodicChecks() {
        return doPeriodicChecks;
    }

    @Override
    public void detachAllHatches() {
        for (var hatch : mSmartInputHatches) {
            hatch.removeWatcher((IHatchWatcher) getMachine());
        }
        mSmartInputHatches.clear();
        doPeriodicChecks = false;
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    protected IECMultiblock validMultiblock(@NotNull IECMultiblock multiblock)
    {
        if (multiblock instanceof IHatchWatcher)
        {
            return multiblock;
        }
        throw new IllegalArgumentException("Multiblock must implement IHatchWatcher, but got: " + multiblock.getClass().getName());
    }
}
