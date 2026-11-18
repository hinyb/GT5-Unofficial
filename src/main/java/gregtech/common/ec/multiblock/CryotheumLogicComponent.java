package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.multiblock.ICryotheumLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;
import gtPlusPlus.xmod.thermalfoundation.fluid.TFFluids;

public class CryotheumLogicComponent extends BaseHatchContainerComponent implements ICryotheumLogicComponent {

    private final List<MTEHatch> mCryotheumHatches = new ArrayList<>();

    @Override
    public boolean tryAttachCryotheum(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        if (part instanceof MTEHatchCustomFluidBase mteHatchCryotheum
            && mteHatchCryotheum.mLockedFluid == TFFluids.fluidCryotheum) {
            if (!mCryotheumHatches.contains(mteHatchCryotheum)) {
                onHatchAttached(mteHatchCryotheum, aBaseCasingIndex);
                mCryotheumHatches.add(mteHatchCryotheum);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mCryotheumHatches);
        mCryotheumHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    public @NotNull List<MTEHatch> getCryotheumHatches() {
        return mCryotheumHatches;
    }
}
