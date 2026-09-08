package gregtech.api.ec.multiblock;

import gregtech.api.ec.IItemOutputLogicComponent;
import gregtech.api.interfaces.IOutputBus;
import gregtech.api.interfaces.IOutputHatch;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.util.GTUtility;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface IMultiblockItemOutputLogicComponent extends IItemOutputLogicComponent, IHatchContainerComponent {
    @NotNull List<MTEHatchOutputBus> getValidHatches();
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachItemOutput(part);
    }
    boolean tryAttachItemOutput(@NotNull IMetaTileEntity part);
    boolean canDumpItemToME(@NotNull List<GTUtility.ItemId> outputs);
    @NotNull List<IOutputBus> getOutputBusses();
}
