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

public interface IFluidInputLogicComponent extends IComponent {
    default boolean depleteInputAtomic(@NotNull FluidStack aLiquid) {
        return depleteInputAtomic(aLiquid, false);
    }

    boolean depleteInputAtomic(@NotNull FluidStack aLiquid, boolean simulate);

    boolean depletePhysicalItemFromHatch(@NotNull ItemStack item, boolean simulate);

    default void appendStoredFluids(@NotNull ArrayList<FluidStack> list) {
        appendStoredFluidsForColor(list, null);
    }

    void appendStoredFluidsForColor(@NotNull ArrayList<FluidStack> list, @Nullable Byte color);

    void appendStoredFluidsFromME(@NotNull Map<GTUtility.FluidId, FluidStack> map);
}
