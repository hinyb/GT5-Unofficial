package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;

public interface IBeamlineInputLogicComponent extends IHatchContainerComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IBeamlineInputLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachBeamlineInput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<MTEHatchInputBeamline> getInputBeamlineHatches();

    default @NotNull Iterable<MTEHatchInputBeamline> getValidInputBeamHatches() {
        return validMTEList(getInputBeamlineHatches());
    }
}
