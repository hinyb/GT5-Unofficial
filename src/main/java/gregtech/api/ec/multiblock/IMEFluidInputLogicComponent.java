package gregtech.api.ec.multiblock;

import java.util.Map;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.util.GTUtility;

public interface IMEFluidInputLogicComponent extends IMultiblockFluidInputLogicComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IMEFluidInputLogicComponent> KEY = ComponentKey.unique();

    @NotNull
    Map<GTUtility.FluidId, FluidStack> getStoredFluidsFromME();
}
