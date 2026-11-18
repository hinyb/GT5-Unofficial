package gregtech.api.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IFluidInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;

public interface IMultiblockFluidInputLogicComponent extends IFluidInputLogicComponent, IHatchContainerComponent {

    ComponentKey.ListKey<IMultiblockFluidInputLogicComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IFluidInputLogicComponent::getFluidInputPriority));

    @NotNull
    List<? extends MTEHatchInput> getFluidInputHatches();

    @NotNull
    default Iterable<? extends MTEHatchInput> getValidFluidInputHatches() {
        return validMTEList(getFluidInputHatches());
    }

    boolean tryAttachFluidInput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);

    short getHatchColors();
}
