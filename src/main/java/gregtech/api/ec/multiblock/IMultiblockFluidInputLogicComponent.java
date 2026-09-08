package gregtech.api.ec.multiblock;

import gregtech.api.ec.IFluidInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface IMultiblockFluidInputLogicComponent extends IFluidInputLogicComponent, IHatchContainerComponent {
    @NotNull List<MTEHatchInput> getValidHatches();
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachFluidInput(part);
    }
    boolean tryAttachFluidInput(@NotNull IMetaTileEntity part);
    boolean depletePhysicalItemFromHatch(@NotNull ItemStack item, boolean simulate);
    void appendStoredFluidsFromME(@NotNull Map<GTUtility.FluidId, FluidStack> map);
    short getHatchColors();
}
