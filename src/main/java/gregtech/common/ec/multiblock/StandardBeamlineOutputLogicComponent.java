package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IBeamlineOutputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.multi.beamcrafting.MTEHatchAdvancedOutputBeamline;
import gtnhlanth.common.hatch.MTEHatchOutputBeamline;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardBeamlineOutputLogicComponent extends BaseMultiblockComponent implements IBeamlineOutputLogicComponent {
    private final List<MTEHatchOutputBeamline> mBeamlineOutputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachBeamlineOutput(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchAdvancedOutputBeamline) {
            return false;
        }
        if (part instanceof MTEHatchOutputBeamline mteHatchOutputBeamline) {
            if (!mBeamlineOutputHatches.contains(mteHatchOutputBeamline))
            {
                mBeamlineOutputHatches.add(mteHatchOutputBeamline);
            }
            return true;
        }
        return false;
    }


    @Override
    public void detachAllHatches()
    {
        mBeamlineOutputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
    }

    @Override
    public @NotNull List<MTEHatchOutputBeamline> getValidHatches() {
        return filterValidMTEs(mBeamlineOutputHatches);
    }
}
