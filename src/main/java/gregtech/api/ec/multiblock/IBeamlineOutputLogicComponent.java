package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEHatchOutputBeamline;

public interface IBeamlineOutputLogicComponent extends IHatchContainerComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IBeamlineOutputLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachBeamlineOutput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<MTEHatchOutputBeamline> getOutputBeamlineHatches();

    default @NotNull Iterable<MTEHatchOutputBeamline> getValidOutputBeamlineHatches() {
        return validMTEList(getOutputBeamlineHatches());
    }
}
