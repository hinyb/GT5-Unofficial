package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMuffler;

public interface IMultiblockPollutionLogicComponent extends IPollutionLogicComponent, IHatchContainerComponent {

    ComponentKey.UniqueKey<IMultiblockPollutionLogicComponent> KEY = ComponentKey.unique();

    @NotNull
    List<MTEHatchMuffler> getPollutionHatches();

    @NotNull
    default Iterable<MTEHatchMuffler> getValidPollutionHatches() {
        return validMTEList(getPollutionHatches());
    }

    boolean tryAttachMuffler(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);
}
