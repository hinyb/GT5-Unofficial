package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.multiblock.IBeamlineInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;

public class BeamlineInputLogicComponent extends BaseHatchContainerComponent implements IBeamlineInputLogicComponent {

    private final List<MTEHatchInputBeamline> mBeamlineInputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachBeamlineInput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEHatchInputBeamline mteHatchInputBeamline) {
            if (!mBeamlineInputHatches.contains(mteHatchInputBeamline)) {
                onHatchAttached(metaTile, baseCasingIndex);
                mBeamlineInputHatches.add(mteHatchInputBeamline);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mBeamlineInputHatches);
        mBeamlineInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    public @NotNull List<MTEHatchInputBeamline> getInputBeamlineHatches() {
        return mBeamlineInputHatches;
    }
}
