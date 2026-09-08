package gregtech.api.ec;

import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IFluidOutputLogicComponent extends IUniqueComponent {
    boolean addOutputAtomic(@NotNull FluidStack stack);
    boolean addOutputAtomic(@NotNull List<FluidStack> stacks);

    int addOutputPartial(@NotNull FluidStack stack, boolean protectExcess);
    default boolean addOutputPartial(@NotNull List<FluidStack> stacks, boolean protectExcess) {
        return addOutputPartial(stacks, protectExcess, null);
    }
    boolean addOutputPartial(@NotNull List<FluidStack> stacks, boolean protectExcess, @Nullable List<FluidStack> remaining);
}
