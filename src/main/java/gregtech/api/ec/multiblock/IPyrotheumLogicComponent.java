package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;

public interface IPyrotheumLogicComponent extends IHatchContainerComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IPyrotheumLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachPyrotheum(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<MTEHatch> getPyrotheumHatches();

    @NotNull
    default Iterable<MTEHatch> getValidPyrotheumHatches() {
        return validMTEList(getPyrotheumHatches());
    }

}
