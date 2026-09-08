package gregtech.api.ec.multiblock;

import gregtech.api.ec.IFluidOutputLogicComponent;
import gregtech.api.interfaces.IOutputHatch;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.util.GTUtility;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface IMultiblockFluidOutputLogicComponent extends IFluidOutputLogicComponent, IHatchContainerComponent {
    @NotNull List<MTEHatchOutput> getValidHatches();
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachFluidOutput(part);
    }
    boolean tryAttachFluidOutput(@NotNull IMetaTileEntity part);
    boolean canDumpFluidToME(@NotNull List<GTUtility.FluidId> outputs);
    /**
     * @param toOutput List of fluids this machine is going to output.
     * @return List of slots available for fluid outputs.
     */
    @NotNull List<IOutputHatch> getOutputHatches(@NotNull List<FluidStack> toOutput);

    @Deprecated
    default @NotNull List<IOutputHatch> getOutputHatches(FluidStack[] toOutput) {
        return getOutputHatches(Arrays.asList(toOutput));
    }
}
