package gregtech.api.ec.multiblock;

import static gregtech.api.enums.ECHatchElement.Dynamo;
import static gregtech.api.enums.ECHatchElement.ExoticDynamo;
import static gregtech.api.enums.ECHatchElement.InputBus;
import static gregtech.api.enums.ECHatchElement.InputHatch;
import static gregtech.api.enums.ECHatchElement.OutputBus;
import static gregtech.api.enums.ECHatchElement.OutputHatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IECMachine;
import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.enums.ECHatchElement;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.metatileentity.IMultiblockController;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IRecipeLockable;
import gregtech.api.interfaces.tileentity.IVoidable;
import gregtech.api.structure.error.ErrorType;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReason;

public interface IECMultiblock extends IECMachine, IMultiblockController, IVoidable, IRecipeLockable {

    boolean isActive();

    void explodeMultiblock();

    void stopMachine(@NotNull ShutDownReason reason);

    @Nullable
    ItemStack getControllerSlot();

    void setControllerSlot(@Nullable ItemStack item);

    long getTotalRunTime();

    boolean isFormed();

    int getStartUpCheckTimer();

    int getApproximateRadius();

    @Nullable
    Vector3f getCenter();

    @FunctionalInterface
    interface HatchAttacher<T extends IHatchContainerComponent> {

        boolean attach(T component, IMetaTileEntity metaTile, int baseCasingIndex);
    }

    default <T extends IHatchContainerComponent> boolean tryAttachHatchToComponent(@NotNull ComponentKey<T> key,
        @Nullable IGregTechTileEntity tile, int baseCasingIndex, @NotNull HatchAttacher<T> attacher) {
        if (tile == null) return false;
        IMetaTileEntity metaTile = tile.getMetaTileEntity();
        if (metaTile == null) return false;
        switch (key) {
            case ComponentKey.ListKey < T > listKey -> {
                for (T component : getComponents(listKey)) {
                    if (attacher.attach(component, metaTile, baseCasingIndex)) {
                        return true;
                    }
                }
            }
            case ComponentKey.UniqueKey < T > uniqueKey -> {
                var component = tryGetComponent(uniqueKey);
                if (component != null) return attacher.attach(component, metaTile, baseCasingIndex);
            }
        }
        return false;
    }

    default <T extends IHatchContainerComponent> int getHatchCountOf(@NotNull ComponentKey.ListKey<T> key,
        @NotNull ToIntFunction<T> counter) {
        int total = 0;
        for (var comp : getComponents(key)) {
            total += counter.applyAsInt(comp);
        }
        return total;
    }

    default <T extends IHatchContainerComponent> int getHatchCountOf(@NotNull ComponentKey.ListKey<T> key,
        Function<T, List<?>> hatchesGetter) {
        return getHatchCountOf(
            key,
            (T comp) -> hatchesGetter.apply(comp)
                .size());
    }

    default <T extends IHatchContainerComponent & IUniqueComponent> int getHatchCountOf(
        @NotNull ComponentKey.UniqueKey<T> key, @NotNull ToIntFunction<T> counter) {
        var comp = tryGetComponent(key);
        return comp != null ? counter.applyAsInt(comp) : 0;
    }

    default <T extends IHatchContainerComponent & IUniqueComponent> int getHatchCountOf(
        @NotNull ComponentKey.UniqueKey<T> key, Function<T, List<?>> hatchesGetter) {
        return getHatchCountOf(
            key,
            (T comp) -> hatchesGetter.apply(comp)
                .size());
    }

    @Override
    default boolean supportsVoidProtection() {
        return hasComponent(IVoidingConfigComponent.KEY);
    }

    @Override
    default boolean supportsSingleRecipeLocking() {
        return hasComponent(IRecipeLockingComponent.KEY);
    }

    /**
     * Ejects a stack. Fluids are only ejected when the whole stack can be ejected into output hatches.
     *
     * @param stack The stack to eject.
     * @return True when the whole stack was ejected, false otherwise.
     */
    default boolean addOutputAtomic(FluidStack stack) {
        return getComponent(IMultiblockFluidOutputLogicComponent.KEY).addOutputAtomic(stack);
    }

    /**
     * Ejects a stack as well as it can. Fluids are ejected regardless of whether the whole stack can fit.
     *
     * @param stack The stack to eject.
     * @return The number of fluids actually ejected.
     */
    default int addOutputPartial(FluidStack stack) {
        return getComponent(IMultiblockFluidOutputLogicComponent.KEY).addOutputPartial(stack, protectsExcessFluid());
    }

    /**
     * Ejects a stack. Items are only ejected when the whole stack can be ejected into output busses.
     *
     * @param stack The stack to eject.
     * @return True when the whole stack was ejected, false otherwise.
     */
    default boolean addOutputAtomic(ItemStack stack) {
        return getComponent(IMultiblockItemOutputLogicComponent.KEY).addOutputAtomic(stack);
    }

    /**
     * Ejects a stack as well as it can. Items are ejected regardless of whether the whole stack can fit.
     *
     * @param stack The stack to eject.
     * @return The number of items actually ejected.
     */
    default int addOutputPartial(ItemStack stack) {
        return getComponent(IMultiblockItemOutputLogicComponent.KEY).addOutputPartial(stack, protectsExcessItem());
    }

    /**
     * Note: only be called inside a recipe processing will contain inputs from ME hatches.
     */
    default ArrayList<FluidStack> getStoredFluids() {
        return getStoredFluidsForColor(null);
    }

    /**
     * Note: only be called inside a recipe processing will contain inputs from ME hatches.
     */
    default ArrayList<FluidStack> getStoredFluidsForColor(@Nullable Byte color) {
        ArrayList<FluidStack> list = new ArrayList<>();
        for (var component : getComponents(IMultiblockFluidInputLogicComponent.KEY)) {
            component.appendStoredFluidsForColor(list, color);
        }
        return list;
    }

    /**
     * Note: only be called inside a recipe processing will contain inputs from ME hatches.
     */
    default ArrayList<ItemStack> getStoredItems() {
        return getStoredItemsForColor(null);
    }

    /**
     * Note: only be called inside a recipe processing will contain inputs from ME hatches.
     */
    default ArrayList<ItemStack> getStoredItemsForColor(@Nullable Byte color) {
        ArrayList<ItemStack> list = new ArrayList<>();
        for (var component : getComponents(IMultiblockItemInputLogicComponent.KEY)) {
            component.appendStoredItemsForColor(list, color);
        }

        ItemStack stackInSlot1 = getControllerSlot();
        if (stackInSlot1 != null && stackInSlot1.getUnlocalizedName()
            .startsWith("gt.integrated_circuit")) list.add(stackInSlot1);
        return list;

    }

    /**
     * Anything that is usually separated off in {@link #getStoredItems()} (like crafting input bus/buffer) is also
     * included here.
     * Note: only be called inside a recipe processing will contain inputs from ME hatches.
     */
    default ArrayList<ItemStack> getAllStoredItems() {
        ArrayList<ItemStack> rList = new ArrayList<>();

        var dualComp = tryGetComponent(IDualInputLogicComponent.KEY);
        if (dualComp != null) {
            dualComp.appendAllStoredItems(rList);
        }

        for (var component : getComponents(IMultiblockItemInputLogicComponent.KEY)) {
            component.appendStoredItemsForColor(rList, null);
        }

        ItemStack stackInSlot1 = getControllerSlot();
        if (stackInSlot1 != null && stackInSlot1.getUnlocalizedName()
            .startsWith("gt.integrated_circuit")) rList.add(stackInSlot1);
        return rList;
    }

    /**
     * Only should be called inside recipe processing.
     */
    default Map<GTUtility.ItemId, ItemStack> getStoredItemsFromME() {
        var comp = tryGetComponent(IMEItemInputLogicComponent.KEY);
        return comp != null ? comp.getStoredItemsFromME() : Collections.emptyMap();
    }

    /**
     * Only should be called inside recipe processing.
     */
    default Map<GTUtility.FluidId, FluidStack> getStoredFluidsFromME() {
        var comp = tryGetComponent(IMEFluidInputLogicComponent.KEY);
        return comp != null ? comp.getStoredFluidsFromME() : Collections.emptyMap();
    }

    default long depleteFluidRange(@NotNull FluidStack fluid, long minAmount, long maxAmount, boolean simulate) {
        if (fluid.getFluid() == null || minAmount <= 0 || maxAmount < minAmount) return 0;
        var comps = getComponents(IMultiblockFluidInputLogicComponent.KEY);
        if (comps.length == 0) return 0;
        if (comps.length == 1) {
            return comps[0].depleteFluidRange(fluid, minAmount, maxAmount, simulate);
        }
        if (minAmount == 1) {
            long remaining = maxAmount;
            for (var comp : comps) {
                remaining -= comp.depleteFluidPartial(fluid, remaining, simulate);
                if (remaining <= 0) return maxAmount;
            }
            return maxAmount - remaining;
        }
        long available = 0;
        for (var comp : comps) {
            available += comp.depleteFluidPartial(fluid, maxAmount - available, true);
            if (available >= maxAmount) break;
        }
        if (available < minAmount) return 0;
        long toDec = Math.min(available, maxAmount);
        if (simulate) return toDec;
        long remaining = toDec;
        for (var comp : comps) {
            long amount = comp.depleteFluidPartial(fluid, remaining, false);
            remaining -= amount;
            if (remaining <= 0) return toDec;
        }
        return toDec - remaining;
    }

    default long depleteFluidPartial(FluidStack aLiquid, long maxAmount, boolean simulate) {
        if (aLiquid.getFluid() == null || maxAmount <= 0) return 0;
        return depleteFluidRange(aLiquid, 1, maxAmount, simulate);
    }

    default boolean depleteFluidAtomic(FluidStack aLiquid, long minAmount, boolean simulate) {
        if (aLiquid.getFluid() == null || minAmount <= 0) return false;
        return depleteFluidRange(aLiquid, minAmount, minAmount, simulate) >= minAmount;
    }

    default @Nullable FluidStack depleteFluidRange(@NotNull Predicate<FluidStack> filter, long minAmount,
        long maxAmount, boolean simulate) {
        if (minAmount <= 0 || maxAmount < minAmount) return null;
        var comps = getComponents(IMultiblockFluidInputLogicComponent.KEY);
        if (comps.length == 0) return null;
        if (comps.length == 1) {
            return comps[0].depleteFluidRange(filter, minAmount, maxAmount, simulate);
        }
        for (var comp : comps) {
            var target = comp
                .depleteFluidAtomic(fluid -> filter.test(fluid) && depleteFluidAtomic(fluid, minAmount, true), 1, true);
            if (target != null) {
                long amount = depleteFluidRange(target, minAmount, maxAmount, simulate);
                if (amount >= minAmount) {
                    // todo support long
                    var result = target.copy();
                    result.amount = (int) Math.min(amount, Integer.MAX_VALUE);
                    return result;
                }
            }
        }
        return null;
    }

    default @Nullable FluidStack depleteInputFluidAtomic(@NotNull Predicate<FluidStack> filter, long minAmount,
        boolean simulate) {
        return depleteFluidRange(filter, minAmount, minAmount, simulate);
    }

    default @Nullable FluidStack depleteInputFluidPartial(@NotNull Predicate<FluidStack> filter, long maxAmount,
        boolean simulate) {
        return depleteFluidRange(filter, 1, maxAmount, simulate);
    }

    default long depleteItemRange(@NotNull ItemStack item, long minAmount, long maxAmount, boolean simulate) {
        if (item.getItem() == null || minAmount <= 0 || maxAmount < minAmount) return 0;
        var comps = getComponents(IItemInputLogicComponent.KEY);
        if (comps.length == 0) return 0;
        if (comps.length == 1) {
            return comps[0].depleteItemRange(item, minAmount, maxAmount, simulate);
        }
        if (minAmount == 1) {
            long remaining = maxAmount;
            for (var comp : comps) {
                remaining -= comp.depleteItemPartial(item, remaining, simulate);
                if (remaining <= 0) return maxAmount;
            }
            return maxAmount - remaining;
        }
        long available = 0;
        for (var comp : comps) {
            available += comp.depleteItemPartial(item, maxAmount - available, true);
            if (available >= maxAmount) break;
        }
        if (available < minAmount) return 0;
        long toDec = Math.min(available, maxAmount);
        if (simulate) return toDec;
        long remaining = toDec;
        for (var comp : comps) {
            long amount = comp.depleteItemPartial(item, remaining, false);
            remaining -= amount;
            if (remaining <= 0) return toDec;
        }
        return toDec - remaining;
    }

    default long depleteItemPartial(@NotNull ItemStack item, long maxAmount, boolean simulate) {
        if (item.getItem() == null || maxAmount <= 0) return 0;
        return depleteItemRange(item, 1, maxAmount, simulate);
    }

    default boolean depleteItemAtomic(@NotNull ItemStack item, long minAmount, boolean simulate) {
        if (item.getItem() == null || minAmount <= 0) return false;
        return depleteItemRange(item, minAmount, minAmount, simulate) >= minAmount;
    }

    default @Nullable ItemStack depleteItemRange(@NotNull Predicate<ItemStack> filter, long minAmount, long maxAmount,
        boolean simulate) {
        if (minAmount <= 0 || maxAmount < minAmount) return null;
        var comps = getComponents(IItemInputLogicComponent.KEY);
        if (comps.length == 0) return null;
        if (comps.length == 1) {
            return comps[0].depleteItemRange(filter, minAmount, maxAmount, simulate);
        }
        for (var comp : comps) {
            var target = comp
                .depleteItemAtomic(item -> filter.test(item) && depleteItemAtomic(item, minAmount, true), 1, true);
            if (target != null) {
                long amount = depleteItemRange(target, minAmount, maxAmount, simulate);
                if (amount >= minAmount) {
                    // todo support long
                    var result = target.copy();
                    result.stackSize = (int) Math.min(amount, Integer.MAX_VALUE);
                    return result;
                }
            }
        }
        return null;
    }

    default @Nullable ItemStack depleteItemAtomic(@NotNull Predicate<ItemStack> filter, long minAmount,
        boolean simulate) {
        return depleteItemRange(filter, minAmount, minAmount, simulate);
    }

    default @Nullable ItemStack depleteItemPartial(@NotNull Predicate<ItemStack> filter, long maxAmount,
        boolean simulate) {
        return depleteItemRange(filter, 1, maxAmount, simulate);
    }

    default void checkHatchMin(List<StructureError> errors, ECHatchElement element, int min) {
        int count = (int) element.count(this);
        if (count < min) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, element, count, min));
        }
    }

    default void checkHatchExact(List<StructureError> errors, ECHatchElement element, int target) {
        int count = (int) element.count(this);
        if (count != target) {
            errors.add(StructureErrors.hatchCount(ErrorType.NOT_MATCH, element, count, target));
        }
    }

    default void checkHatchMax(List<StructureError> errors, ECHatchElement element, int max) {
        int count = (int) element.count(this);
        if (count > max) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_MANY, element, count, max));
        }
    }

    default void checkCasingMin(List<StructureError> errors, int current, int required) {
        if (current < required) {
            errors.add(StructureErrors.missingCasings(current, required));
        }
    }

    default void checkHasEnergyHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.Energy, 1);
    }

    default void checkOneEnergyHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.Energy, 1);
    }

    default void checkHasDynamoHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.Dynamo, 1);
    }

    default void checkHasMaintenanceHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.Maintenance, 1);
    }

    default void checkOneMaintenanceHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.Maintenance, 1);
    }

    default void checkOneDynamoHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.Dynamo, 1);
    }

    default void checkHasMufflerHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.Muffler, 1);
    }

    default void checkOneMufflerHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.Muffler, 1);
    }

    default void checkHasInputBus(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.InputBus, 1);
    }

    default void checkOneInputBus(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.InputBus, 1);
    }

    default void checkHasInputHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.InputHatch, 1);
    }

    default void checkOneInputHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.InputHatch, 1);
    }

    default void checkHasOutputBus(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.OutputBus, 1);
    }

    default void checkOneOutputBus(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.OutputBus, 1);
    }

    default void checkHasOutputHatch(List<StructureError> errors) {
        checkHatchMin(errors, ECHatchElement.OutputHatch, 1);
    }

    default void checkOneOutputHatch(List<StructureError> errors) {
        checkHatchExact(errors, ECHatchElement.OutputHatch, 1);
    }

    default void checkHasAnyInput(List<StructureError> errors) {
        if ((InputBus.count(this) + InputHatch.count(this)) == 0) {
            errors.add(StructureErrors.of("GT5U.gui.text.structure_error.missing_any_input"));
        }
    }

    default void checkHasAnyOutput(List<StructureError> errors) {
        if ((OutputBus.count(this) + OutputHatch.count(this)) == 0) {
            errors.add(StructureErrors.of("GT5U.gui.text.structure_error.missing_any_output"));
        }
    }

    default void checkHasAnyEnergy(List<StructureError> errors) {
        if (getHatchCountOf(
            IMultiblockEnergyInputLogicComponent.KEY,
            IMultiblockEnergyInputLogicComponent::getEnergyHatches) == 0) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, ECHatchElement.Energy, 0, 1));
        }
    }

    default void checkHasAnyDynamo(List<StructureError> errors) {
        if ((Dynamo.count(this) + ExoticDynamo.count(this)) == 0) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, ECHatchElement.Dynamo, 0, 1));
        }
    }

    default void checkOneEnergyHatchMaybeExotic(List<StructureError> errors) {
        int count = getHatchCountOf(
            IMultiblockEnergyInputLogicComponent.KEY,
            IMultiblockEnergyInputLogicComponent::getEnergyHatches);
        if (count == 0) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, ECHatchElement.Energy, 0, 1));
        } else if (count > 1) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_MANY, ECHatchElement.Energy, count, 1));
        }
    }
}
