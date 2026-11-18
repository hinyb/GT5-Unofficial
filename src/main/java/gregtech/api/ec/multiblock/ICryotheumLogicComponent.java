package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;

public interface ICryotheumLogicComponent extends IHatchContainerComponent, IUniqueComponent {

    ComponentKey.UniqueKey<ICryotheumLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachCryotheum(IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<MTEHatch> getCryotheumHatches();

    default @NotNull Iterable<MTEHatch> getValidCryotheumHatches() {
        return validMTEList(getCryotheumHatches());
    }
}
