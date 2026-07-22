package gregtech.common.ec.multiblock;

import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IRecipeProcessAwareComponent;
import gregtech.api.ec.ISlotUpdateAwareComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidInputLogicComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchInputDebug;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardFluidInputLogicComponent implements IMultiblockFluidInputLogicComponent, ISlotUpdateAwareComponent, IRecipeProcessAwareComponent {

    private final BooleanSupplier filterOnRecipeMap;
    StandardFluidInputLogicComponent(BooleanSupplier filterOnRecipeMap)
    {
        this.filterOnRecipeMap = filterOnRecipeMap;
    }
    private final ArrayList<MTEHatchInput> mInputHatches = new ArrayList<>();

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part) {
        if (part instanceof IDualInputHatch hatch) {
            return false;
        }
        if (part instanceof MTEHatchInput hatch) {
            setHatchRecipeMap(hatch);
            if (!mInputHatches.contains(hatch))
            {
                mInputHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches()
    {
        mInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    @Override
    public @NotNull List<MTEHatchInput> getValidHatches() {
        return filterValidMTEs(mInputHatches);
    }

    private void setHatchRecipeMap(MTEHatchInput hatch)
    {
        if (filterOnRecipeMap.getAsBoolean() && multiblock != null)
        {
            hatch.mRecipeMap = multiblock.getComponent(IRecipeLogicComponent.class).getRecipeMap();
        }
    }

    @Override
    public boolean depleteInputAtomic(@NotNull FluidStack aLiquid, boolean simulate) {
        if (!GTUtility.isStackValid(aLiquid)) return false;
        int fluidCost = aLiquid.amount;
        for (MTEHatchInput tHatch : getValidHatches()) {
            setHatchRecipeMap(tHatch);
            FluidStack tLiquid = tHatch.drain(ForgeDirection.UNKNOWN, aLiquid, false);
            if (tLiquid == null) continue;
            if (tLiquid.amount >= aLiquid.amount) {
                if (simulate) {
                    return true;
                }
                tLiquid = tHatch.drain(ForgeDirection.UNKNOWN, aLiquid, true);
                return tLiquid.amount >= aLiquid.amount;
            }
            fluidCost -= tLiquid.amount;
            if (fluidCost <= 0) break;
        }
        // Enough fluid is present spread through multiple hatches. Drain requested amount
        if (fluidCost <= 0) {
            if (simulate) return true;
            fluidCost = aLiquid.amount;
            for (MTEHatchInput tHatch : getValidHatches()) {
                FluidStack tLiquid = tHatch.drain(ForgeDirection.UNKNOWN, aLiquid, fluidCost, true);
                if (tLiquid == null) continue;
                fluidCost -= tLiquid.amount;
                if (fluidCost == 0) return true;
            }
        }
        return false;
    }

    @Override
    public boolean depletePhysicalItemFromHatch(@NotNull ItemStack aStack, boolean simulate) {
        if (GTUtility.isStackInvalid(aStack)) return false;
        FluidStack aLiquid = GTUtility.getFluidForFilledItem(aStack, true);
        if (aLiquid != null) return depleteInputAtomic(aLiquid, simulate);
        for (MTEHatchInput tHatch : getValidHatches()) {
            setHatchRecipeMap(tHatch);
            final IGregTechTileEntity baseMetaTileEntity = tHatch.getBaseMetaTileEntity();
            ItemStack stackInFirstSlot = baseMetaTileEntity.getStackInSlot(0);
            if (GTUtility.areStacksEqual(aStack, stackInFirstSlot)) {
                if (stackInFirstSlot.stackSize >= aStack.stackSize) {
                    if (simulate) return true;
                    baseMetaTileEntity.decrStackSize(0, aStack.stackSize);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendStoredFluidsForColor(@NotNull ArrayList<FluidStack> rList, @Nullable Byte color) {
        Map<Fluid, FluidStack> inputsFromME = new HashMap<>();
        for (MTEHatchInput tHatch : getValidHatches()) {
            byte hatchColor = tHatch.getColor();
            if (color != null && hatchColor != -1 && hatchColor != color) continue;
            setHatchRecipeMap(tHatch);
            switch (tHatch) {
                case MTEHatchMultiInput multiInputHatch -> {
                    for (FluidStack tFluid : multiInputHatch.getStoredFluid()) {
                        if (tFluid != null) {
                            rList.add(tFluid);
                        }
                    }
                }
                case MTEHatchInputME meHatch -> {
                    for (FluidStack fluidStack : meHatch.getStoredFluids()) {
                        if (fluidStack != null) {
                            // Prevent the same fluid from different ME hatches from being recognized
                            inputsFromME.put(fluidStack.getFluid(), fluidStack);
                        }
                    }
                }
                case MTEHatchInputDebug debugHatch -> {
                    for (FluidStack fluid : debugHatch.getFluidList()) {
                        if (fluid != null) {
                            FluidStack stack = fluid.copy();
                            stack.amount = Integer.MAX_VALUE;
                            rList.add(stack);
                        }
                    }
                }
                default -> {
                    FluidStack fillableStack = tHatch.getFillableStack();
                    if (fillableStack != null) {
                        rList.add(fillableStack);
                    }
                }
            }
        }

        if (!inputsFromME.isEmpty()) {
            rList.addAll(inputsFromME.values());
        }
    }

    @Override
    public void appendStoredFluidsFromME(@NotNull Map<GTUtility.FluidId, FluidStack> fluidsFromME) {
        for (MTEHatchInput tHatch : getValidHatches()) {
            if (tHatch instanceof MTEHatchInputME meHatch) {
                for (FluidStack fluid : meHatch.getStoredFluids()) {
                    if (fluid != null) {
                        // Prevent the same fluid from different ME hatches from being recognized
                        fluidsFromME.put(GTUtility.FluidId.createNoCopy(fluid), fluid);
                    }
                }
            }
        }
    }

    @Override
    public void startRecipeProcessing() {
        for (MTEHatchInput hatch : getValidHatches()) {
            if (hatch instanceof IRecipeProcessingAwareHatch aware) {
                aware.startRecipeProcessing();
            }
        }
    }

    @Override
    public CheckRecipeResult endRecipeProcessing() {
        CheckRecipeResult worstResult = CheckRecipeResultRegistry.SUCCESSFUL;
        for (MTEHatchInput hatch : getValidHatches()) {
            if (hatch instanceof IRecipeProcessingAwareHatch aware) {
                // todo remove controller
                var result = aware.endRecipeProcessing(null);
                if (!result.wasSuccessful())
                {
                    worstResult = result;
                }
            }
        }
        return worstResult;
    }

    @Override
    public void updateSlots() {
        for (var tHatch : getValidHatches()) tHatch.updateSlots();
    }

    private @Nullable IECMultiblock multiblock = null;
    @Override
    public void onAddToMultiblock(@NotNull IECMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public @Nullable IECMultiblock getMachine() {
        return multiblock;
    }
}
