package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.multiblock.IPyrotheumLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;
import gtPlusPlus.xmod.thermalfoundation.fluid.TFFluids;

public class PyrotheumLogicComponent extends BaseHatchContainerComponent implements IPyrotheumLogicComponent {

    private final List<MTEHatch> mPyrotheumHatches = new ArrayList<>();

    @Override
    public boolean tryAttachPyrotheum(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEHatchCustomFluidBase mteHatchPyrotheum
            && mteHatchPyrotheum.mLockedFluid == TFFluids.fluidPyrotheum) {
            if (!mPyrotheumHatches.contains(mteHatchPyrotheum)) {
                onHatchAttached(mteHatchPyrotheum, baseCasingIndex);
                mPyrotheumHatches.add(mteHatchPyrotheum);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mPyrotheumHatches);
        mPyrotheumHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    public @NotNull List<MTEHatch> getPyrotheumHatches() {
        return mPyrotheumHatches;
    }
}
