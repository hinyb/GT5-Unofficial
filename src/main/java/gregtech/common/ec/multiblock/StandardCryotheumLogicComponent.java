package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.ICryotheumLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;
import gtPlusPlus.xmod.thermalfoundation.fluid.TFFluids;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardCryotheumLogicComponent extends BaseMultiblockComponent implements ICryotheumLogicComponent {
    private final List<MTEHatch> mCryotheumHatches = new ArrayList<>();

    @Override
    public boolean tryAttachCryotheum(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchCustomFluidBase mteHatchCryotheum && mteHatchCryotheum.mLockedFluid == TFFluids.fluidCryotheum) {
            if (!mCryotheumHatches.contains(mteHatchCryotheum))
            {
                mCryotheumHatches.add(mteHatchCryotheum);
            }
            return true;
        }
        return false;
    }


    @Override
    public void detachAllHatches()
    {
        mCryotheumHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
    }

    @Override
    public @NotNull List<MTEHatch> getValidHatches() {
        return filterValidMTEs(mCryotheumHatches);
    }
}
