package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.multiblock.IInputFocusLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gtnhlanth.common.hatch.MTEBusInputFocus;

public class InputFocusLogicComponent extends BaseHatchContainerComponent implements IInputFocusLogicComponent {

    private final List<MTEBusInputFocus> mInputFocusHatches = new ArrayList<>();

    @Override
    public boolean tryAttachInputFocus(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEBusInputFocus mteBusInputFocus) {
            if (!mInputFocusHatches.contains(mteBusInputFocus)) {
                onHatchAttached(metaTile, baseCasingIndex);
                mInputFocusHatches.add(mteBusInputFocus);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mInputFocusHatches);
        mInputFocusHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {}

    @Override
    public @NotNull List<MTEBusInputFocus> getInputFocusHatches() {
        return mInputFocusHatches;
    }
}
