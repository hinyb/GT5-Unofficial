package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IInputFocusLogicComponent;
import gregtech.api.ec.multiblock.IInputFocusLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gtnhlanth.common.hatch.MTEBusInputFocus;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardInputFocusLogicComponent extends BaseMultiblockComponent implements IInputFocusLogicComponent {
    private final List<MTEBusInputFocus> mInputFocusHatches = new ArrayList<>();

    @Override
    public boolean tryAttachInputFocus(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEBusInputFocus mteBusInputFocus) {
            if (!mInputFocusHatches.contains(mteBusInputFocus))
            {
                mInputFocusHatches.add(mteBusInputFocus);
            }
            return true;
        }
        return false;
    }


    @Override
    public void detachAllHatches()
    {
        mInputFocusHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
    }

    @Override
    public @NotNull List<MTEBusInputFocus> getValidHatches() {
        return filterValidMTEs(mInputFocusHatches);
    }
}
