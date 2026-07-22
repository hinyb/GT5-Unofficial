package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IDualInputLogicComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.tileentities.machines.IDualInputHatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static gregtech.api.util.GTUtility.*;

public class StandardDualInputLogicComponent implements IDualInputLogicComponent {
    private final List<IDualInputHatch> mDualInputHatches = new ArrayList<>();
    private final BooleanSupplier supportsMEBuffer;

    public StandardDualInputLogicComponent(BooleanSupplier supportsMEBuffer) {
        this.supportsMEBuffer = supportsMEBuffer;
    }

    @Override
    public @NotNull List<IDualInputHatch> getValidHatches() {
        return uncheckFilterValidMTEs(mDualInputHatches);
    }

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part)
    {
        if (!supportsMEBuffer.getAsBoolean()) return false;
        if (part instanceof IDualInputHatch hatch) {
            if (!mDualInputHatches.contains(hatch))
            {
                mDualInputHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mDualInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
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
