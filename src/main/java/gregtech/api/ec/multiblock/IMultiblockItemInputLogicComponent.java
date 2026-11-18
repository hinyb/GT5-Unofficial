package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;

public interface IMultiblockItemInputLogicComponent extends IItemInputLogicComponent, IHatchContainerComponent {

    ComponentKey.ListKey<IMultiblockItemInputLogicComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IItemInputLogicComponent::getItemInputPriority));

    boolean tryAttachItemInput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    @NotNull
    List<? extends MTEHatchInputBus> getItemInputHatches();

    @NotNull
    default Iterable<? extends MTEHatchInputBus> getValidItemInputHatches() {
        return validMTEList(getItemInputHatches());
    }

    short getHatchColors();
}
