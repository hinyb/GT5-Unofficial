package gregtech.api.ec;

import java.util.List;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFluidOutputLogicComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IFluidOutputLogicComponent> KEY = ComponentKey.unique();

    boolean addOutputAtomic(@NotNull FluidStack stack);

    boolean addOutputAtomic(@NotNull List<FluidStack> stacks);

    int addOutputPartial(@NotNull FluidStack stack, boolean protectExcess);

    default boolean addOutputPartial(@NotNull List<FluidStack> stacks, boolean protectExcess) {
        return addOutputPartial(stacks, protectExcess, null);
    }

    boolean addOutputPartial(@NotNull List<FluidStack> stacks, boolean protectExcess,
        @Nullable List<@Nullable FluidStack> remaining);
}
