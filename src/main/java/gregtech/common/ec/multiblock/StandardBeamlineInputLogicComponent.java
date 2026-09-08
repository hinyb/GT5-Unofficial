package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IBeamlineInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardBeamlineInputLogicComponent extends BaseMultiblockComponent implements IBeamlineInputLogicComponent {
    private final List<MTEHatchInputBeamline> mBeamlineInputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachBeamlineInput(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchInputBeamline mteHatchInputBeamline) {
            if (!mBeamlineInputHatches.contains(mteHatchInputBeamline))
            {
                mBeamlineInputHatches.add(mteHatchInputBeamline);
            }
            return true;
        }
        return false;
    }


    @Override
    public void detachAllHatches()
    {
        mBeamlineInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
    }

    @Override
    public @NotNull List<MTEHatchInputBeamline> getValidHatches() {
        return filterValidMTEs(mBeamlineInputHatches);
    }
}
