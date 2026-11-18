package gregtech.common.ec.multiblock;

import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.me.GridAccessException;
import gregtech.api.ec.IFluidInputLogicComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IRecipeProcessAwareComponent;
import gregtech.api.ec.ISlotUpdateAwareComponent;
import gregtech.api.ec.multiblock.IHatchConfigCopyableComponent;
import gregtech.api.ec.multiblock.IMEFluidInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseHatchContainerComponent;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MEFluidInputLogicComponent extends BaseHatchContainerComponent implements IMEFluidInputLogicComponent,
    ISlotUpdateAwareComponent, IRecipeProcessAwareComponent, IHatchConfigCopyableComponent {

    private final @NotNull BooleanSupplier filterOnRecipeMap;

    public MEFluidInputLogicComponent(@NotNull BooleanSupplier filterOnRecipeMap) {
        this.filterOnRecipeMap = filterOnRecipeMap;
    }

    private final ArrayList<MTEHatchInputME> mInputHatches = new ArrayList<>();
    private boolean inRecipeProcessing = false;

    @Override
    public boolean tryAttachFluidInput(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof IDualInputHatch) {
            return false;
        }
        if (metaTile instanceof MTEHatchInputME hatch) {
            hatch.mRecipeMap = getRecipeMap();

            if (!mInputHatches.contains(hatch)) {
                onHatchAttached(hatch, baseCasingIndex);
                mInputHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        clearCraftingIcons(mInputHatches);
        mInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
        for (MetaTileEntity tTileEntity : getValidFluidInputHatches()) {
            tTileEntity.getBaseMetaTileEntity()
                .doExplosion(power);
        }
    }

    @Override
    public @NotNull List<? extends MTEHatchInput> getFluidInputHatches() {
        return mInputHatches;
    }

    private @Nullable RecipeMap<?> getRecipeMap() {
        return recipeLogic != null && filterOnRecipeMap.getAsBoolean() ? recipeLogic.getRecipeMap() : null;
    }

    @Override
    public int getFluidInputPriority() {
        return FluidInputPriority.ME_BUS.priority;
    }

    @Override
    public long depleteFluidRange(@NotNull FluidStack stack, long minAmount, long maxAmount, boolean simulate) {
        if (stack.getFluid() == null || minAmount <= 0 || maxAmount < minAmount || mInputHatches.isEmpty()) return 0;
        boolean needManageLifecycle = !inRecipeProcessing;
        if (needManageLifecycle) startRecipeProcessing();
        long amount = 0;
        try {
            List<FluidStack> stored = getStoredFluidsForColor(null);
            amount = IFluidInputLogicComponent.depleteFromFluidList(stored, stack, minAmount, maxAmount, simulate);
        } finally {
            if (needManageLifecycle) {
                var checkResult = endRecipeProcessing();
                if (!simulate && amount > 0) {
                    updateSlots();
                }
                if (!checkResult.wasSuccessful()) {
                    amount = 0;
                }
            }
        }
        return amount;
    }

    @Override
    public @Nullable FluidStack depleteFluidRange(@NotNull Predicate<FluidStack> filter, long minAmount, long maxAmount,
        boolean simulate) {
        if (minAmount <= 0 || maxAmount < minAmount || mInputHatches.isEmpty()) return null;
        boolean needManageLifecycle = !inRecipeProcessing;
        if (needManageLifecycle) startRecipeProcessing();
        FluidStack result = null;
        try {
            List<FluidStack> stored = getStoredFluidsForColor(null);
            result = IFluidInputLogicComponent.depleteFromFluidList(stored, filter, minAmount, maxAmount, simulate);
        } finally {
            if (needManageLifecycle) {
                var checkResult = endRecipeProcessing();
                if (!simulate && result != null) {
                    updateSlots();
                }
                if (!checkResult.wasSuccessful()) {
                    result = null;
                }
            }
        }
        return result;
    }

    private final Set<GTUtility.FluidId> seenFluids = new ObjectOpenHashSet<>();

    private static @Nullable Object getMEGridKey(@NotNull MTEHatchInputME meInput) {
        try {
            return meInput.getProxy()
                .getGrid();
        } catch (GridAccessException e) {
            return null;
        }
    }

    // todo support hatches from different subnetworks
    @Override
    public void appendStoredFluidsForColor(@NotNull List<FluidStack> rList, @Nullable Byte color) {
        if (mInputHatches.isEmpty()) return;
        var recipeMap = getRecipeMap();
        try {
            for (var tHatch : validMTEList(mInputHatches)) {
                byte hatchColor = tHatch.getColor();
                if (color != null && hatchColor != -1 && hatchColor != color) continue;
                tHatch.mRecipeMap = recipeMap;
                for (FluidStack fluidStack : tHatch.getStoredFluids()) {
                    if (fluidStack != null) {
                        // Prevent the same fluid from different ME hatches from being recognized
                        if (seenFluids.add(GTUtility.FluidId.createNoCopy(fluidStack))) {
                            rList.add(fluidStack);
                        }
                    }
                }
            }
        } finally {
            seenFluids.clear();
        }
    }

    @Override
    public short getHatchColors() {
        short hatchColors = 0;
        for (var bus : getValidFluidInputHatches()) hatchColors |= (short) (1 << bus.getColor());
        return hatchColors;
    }

    @Override
    public void startRecipeProcessing() {
        inRecipeProcessing = true;
        for (MTEHatchInput hatch : getValidFluidInputHatches()) {
            if (hatch instanceof IRecipeProcessingAwareHatch aware) {
                aware.startRecipeProcessing();
            }
        }
    }

    @Override
    public @NotNull CheckRecipeResult endRecipeProcessing() {
        inRecipeProcessing = false;
        CheckRecipeResult worstResult = CheckRecipeResultRegistry.SUCCESSFUL;
        for (MTEHatchInput hatch : getValidFluidInputHatches()) {
            if (hatch instanceof IRecipeProcessingAwareHatch aware) {
                var result = aware.endRecipeProcessing(getMachine());
                if (!result.wasSuccessful()) {
                    worstResult = result;
                }
            }
        }
        return worstResult;
    }

    @Override
    public void updateSlots() {
        for (var tHatch : getValidFluidInputHatches()) tHatch.updateSlots();
    }

    private @Nullable IRecipeLogicComponent recipeLogic;

    @Override
    public void onComponentsReady() {
        super.onComponentsReady();
        recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
    }

    @Override
    public @NotNull Map<String, @NotNull Collection<? extends MTEHatch>> getCopyableHatches() {
        return Collections.singletonMap("mInputHatches", mInputHatches);
    }

    @Override
    public @NotNull Map<GTUtility.FluidId, FluidStack> getStoredFluidsFromME() {
        if (mInputHatches.isEmpty()) return Collections.emptyMap();
        var recipeMap = getRecipeMap();
        Map<GTUtility.FluidId, FluidStack> result = new HashMap<>();
        for (MTEHatchInput tHatch : getValidFluidInputHatches()) {
            tHatch.mRecipeMap = recipeMap;
            var meHatch = (MTEHatchInputME) tHatch;
            for (FluidStack fluidStack : meHatch.getStoredFluids()) {
                if (fluidStack != null) {
                    result.put(GTUtility.FluidId.createNoCopy(fluidStack), fluidStack);
                }
            }
        }
        return result;
    }
}
