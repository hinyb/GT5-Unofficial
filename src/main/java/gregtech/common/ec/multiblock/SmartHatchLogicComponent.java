package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.ISmartHatchLogicComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.tileentities.machines.IHatchWatcher;
import gregtech.common.tileentities.machines.ISmartInputHatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SmartHatchLogicComponent implements ISmartHatchLogicComponent {
    private final List<ISmartInputHatch> mSmartInputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part) {
        if (watcher == null)
        {
            throw new IllegalStateException("Can't attach part before setting machine");
        }
        if (part instanceof ISmartInputHatch hatch) {
            mSmartInputHatches.add(hatch);
            hatch.addWatcher(watcher);
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        if (watcher != null) {
            for (var hatch : mSmartInputHatches) {
                hatch.removeWatcher(watcher);
            }
        }
        mSmartInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    private @Nullable IHatchWatcher watcher = null;

    @Override
    public void onAddToMultiblock(@NotNull IECMultiblock multiblock) {
        if (multiblock instanceof IHatchWatcher w)
        {
            this.watcher = w;
            return;
        }
        throw new IllegalArgumentException("Multiblock must implement IHatchWatcher, but got: " + multiblock.getClass().getName());
    }

    @Override
    public @Nullable IECMultiblock getMachine() {
        return (IECMultiblock) watcher;
    }
}
