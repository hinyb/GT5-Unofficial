package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEBusInputFocus;

public interface IInputFocusLogicComponent extends IHatchContainerComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IInputFocusLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachInputFocus(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<MTEBusInputFocus> getInputFocusHatches();

    @NotNull
    default Iterable<MTEBusInputFocus> getValidInputFocusHatches() {
        return validMTEList(getInputFocusHatches());
    }
}
