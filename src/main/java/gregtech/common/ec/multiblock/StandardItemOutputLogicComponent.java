package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IMultiblockItemOutputLogicComponent;
import gregtech.api.interfaces.IOutputBus;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.metatileentity.implementations.MTEHatchVoidBus;
import gregtech.api.util.FluidEjectionHelper;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemEjectionHelper;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.outputme.MTEHatchOutputBusME;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;
import static gregtech.api.util.GTUtility.validMTEList;

public class StandardItemOutputLogicComponent extends BaseMultiblockComponent implements IMultiblockItemOutputLogicComponent {
    private final List<MTEHatchOutputBus> mOutputBusses = new ArrayList<>();

    @Override
    public boolean canDumpItemToME(@NotNull List<GTUtility.ItemId> outputs) {
        List<MTEHatchOutputBusME> busses = GTUtility.getMTEsOfType(mOutputBusses, MTEHatchOutputBusME.class);
        List<MTEHatchOutputBusME> filteredBusses = new ArrayList<>();
        for (MTEHatchOutputBusME bus : busses) {
            if (!bus.hasPhysicalSpace() || bus.getCheckMode()) continue;
            if (!bus.isFiltered()) return true;
            filteredBusses.add(bus);
        }
        for (GTUtility.ItemId output : outputs) {
            boolean handled = false;
            for (MTEHatchOutputBusME busME : filteredBusses) {
                // If the bus is unfiltered or is filtered to this item, we can eject the stack fully
                // We don't care about bus ordering here because we're just checking if it's possible
                if (busME.isFilteredToItem(output)) {
                    handled = true;
                    break;
                }
            }
            if (!handled) return false;
        }
        return true;
    }

    @Override
    public @NotNull List<IOutputBus> getOutputBusses() {
        List<IOutputBus> totalBusses = new ArrayList<>(filterValidMTEs(mOutputBusses));
        totalBusses.removeIf(bus -> bus instanceof MTEHatchVoidBus voidBus && !voidBus.isLocked());
        return totalBusses;
    }

    @Override
    public boolean addOutputAtomic(@NotNull ItemStack stack) {
        if (GTUtility.isStackInvalid(stack)) return false;

        int initial = stack.stackSize;

        ItemEjectionHelper ejectionHelper = new ItemEjectionHelper(getOutputBusses(), true);
        ejectionHelper.ejectStack(stack);

        if (stack.stackSize == 0) {
            ejectionHelper.commit();
            return true;
        } else {
            // Restore the original stack size because we didn't end up doing anything.
            stack.stackSize = initial;
            return false;
        }
    }

    @Override
    public boolean addOutputAtomic(@NotNull List<ItemStack> stacks) {
        if (stacks.isEmpty()) return true;
        var ejectionHelper = new ItemEjectionHelper(getOutputBusses(), true);
        int ejected = ejectionHelper.ejectItems(stacks, 1, null);
        if (ejected == 1) {
            ejectionHelper.commit();
            return true;
        }
        return false;
    }

    @Override
    public int addOutputPartial(@NotNull ItemStack stack, boolean protectExcess) {
        if (!GTUtility.isStackValid(stack)) return 0;

        var ejectionHelper = new ItemEjectionHelper(getOutputBusses(), protectExcess);
        var list = Collections.singletonList(GTUtility.copyAmount(1, stack));
        int ejected = ejectionHelper.ejectItems(list, stack.stackSize);
        ejectionHelper.commit();
        return ejected;
    }

    @Override
    public boolean addOutputPartial(@NotNull List<ItemStack> stacks, boolean protectExcess, @Nullable List<ItemStack> remaining) {
        if (stacks.isEmpty()) return true;
        ItemEjectionHelper ejectionHelper = new ItemEjectionHelper(getOutputBusses(), protectExcess);
        int ejected = ejectionHelper.ejectItems(stacks, 1, remaining);
        ejectionHelper.commit();

        return ejected == 1;
    }

    @Override
    public @NotNull List<MTEHatchOutputBus> getValidHatches() {
        return filterValidMTEs(mOutputBusses);
    }

    @Override
    public boolean tryAttachItemOutput(@NotNull IMetaTileEntity part) {
        if (part instanceof MTEHatchOutputBus hatch) {
            if (!mOutputBusses.contains(hatch))
            {
                mOutputBusses.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mOutputBusses.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }
}
