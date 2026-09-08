package gregtech.api.ec;

import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IFluidInputLogicComponent extends IUniqueComponent {
    default boolean depleteInputAtomic(@NotNull FluidStack aLiquid) {
        return depleteInputAtomic(aLiquid, false);
    }
    boolean depleteInputAtomic(@NotNull FluidStack aLiquid, boolean simulate);
    default void appendStoredFluids(@NotNull List<FluidStack> list) {
        appendStoredFluidsForColor(list, null);
    }
    void appendStoredFluidsForColor(@NotNull List<FluidStack> list, @Nullable Byte color);
    default List<FluidStack> getStoredFluidsForColor(@Nullable Byte color)
    {
        List<FluidStack> list = new ArrayList<>();
        appendStoredFluidsForColor(list, color);
        return list;
    }
}
