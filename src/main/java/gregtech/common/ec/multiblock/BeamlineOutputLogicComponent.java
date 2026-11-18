package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.multiblock.IBeamlineOutputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gregtech.common.tileentities.machines.multi.beamcrafting.MTEHatchAdvancedOutputBeamline;
import gtnhlanth.common.hatch.MTEHatchOutputBeamline;

public class BeamlineOutputLogicComponent extends BaseHatchContainerComponent implements IBeamlineOutputLogicComponent {

    private final List<MTEHatchOutputBeamline> mBeamlineOutputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachBeamlineOutput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEHatchAdvancedOutputBeamline) {
            return false;
        }
        if (metaTile instanceof MTEHatchOutputBeamline mteHatchOutputBeamline) {
            if (!mBeamlineOutputHatches.contains(mteHatchOutputBeamline)) {
                onHatchAttached(mteHatchOutputBeamline, baseCasingIndex);
                mBeamlineOutputHatches.add(mteHatchOutputBeamline);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mBeamlineOutputHatches);
        mBeamlineOutputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    public @NotNull List<MTEHatchOutputBeamline> getOutputBeamlineHatches() {
        return mBeamlineOutputHatches;
    }
}
