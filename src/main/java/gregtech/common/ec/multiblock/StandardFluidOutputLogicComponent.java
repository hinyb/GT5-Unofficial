package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IMultiblockFluidOutputLogicComponent;
import gregtech.api.interfaces.IOutputHatch;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchVoid;
import gregtech.api.util.FluidEjectionHelper;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.outputme.MTEHatchOutputME;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;
import static gregtech.api.util.GTUtility.validMTEList;

public class StandardFluidOutputLogicComponent extends BaseMultiblockComponent implements IMultiblockFluidOutputLogicComponent {
    private final List<MTEHatchOutput> mOutputHatches = new ArrayList<>();

    @Override
    public boolean canDumpFluidToME(@NotNull List<GTUtility.FluidId> outputs) {
        List<MTEHatchOutputME> hatches = GTUtility.getMTEsOfType(mOutputHatches, MTEHatchOutputME.class);
        List<MTEHatchOutputME> filteredHatches = new ArrayList<>();
        for (MTEHatchOutputME bus : hatches) {
            if (!bus.hasPhysicalSpace() || bus.getCheckMode()) continue;
            if (!bus.isFiltered()) return true;
            filteredHatches.add(bus);
        }
        for (GTUtility.FluidId output : outputs) {
            boolean handled = false;
            for (MTEHatchOutputME busME : filteredHatches) {
                if (busME.isFilteredToFluid(output)) {
                    handled = true;
                    break;
                }
            }
            if (!handled) return false;
        }
        return true;
    }

    @Override
    public @NotNull List<IOutputHatch> getOutputHatches(@NotNull List<FluidStack> toOutput) {
        ArrayList<IOutputHatch> totalHatches = new ArrayList<>(filterValidMTEs(mOutputHatches));
        totalHatches.removeIf(hatch -> hatch instanceof MTEHatchVoid voidHatch && !voidHatch.isFluidLocked());
        return totalHatches;
    }

    @Override
    public boolean addOutputAtomic(@NotNull FluidStack stack) {
        if (!GTUtility.isStackValid(stack)) return false;

        int initial = stack.amount;

        FluidEjectionHelper ejectionHelper = new FluidEjectionHelper(getOutputHatches(List.of(stack)), true);
        ejectionHelper.ejectStack(stack);

        if (stack.amount == 0) {
            ejectionHelper.commit();
            return true;
        } else {
            // Restore the original stack size because we didn't end up doing anything.
            stack.amount = initial;
            return false;
        }
    }

    @Override
    public boolean addOutputAtomic(@NotNull List<FluidStack> stacks) {
        if (stacks.isEmpty()) return true;
        var ejectionHelper = new FluidEjectionHelper(getOutputHatches(stacks), true);
        int ejected = ejectionHelper.ejectFluids(stacks, 1, null);
        if (ejected == 1) {
            ejectionHelper.commit();
            return true;
        }
        return false;
    }

    @Override
    public int addOutputPartial(@NotNull FluidStack stack, boolean protectExcess) {
        if (!GTUtility.isStackValid(stack)) return 0;

        FluidEjectionHelper ejectionHelper = new FluidEjectionHelper(getOutputHatches(List.of(stack)), protectExcess);
        var list = Collections.singletonList(GTUtility.copyAmount(1, stack));
        int ejected = ejectionHelper.ejectFluids(list, stack.amount);
        ejectionHelper.commit();
        return ejected;
    }

    @Override
    public boolean addOutputPartial(@NotNull List<FluidStack> stacks, boolean protectExcess, @Nullable List<FluidStack> remaining) {
        if (stacks.isEmpty()) return true;
        FluidEjectionHelper ejectionHelper = new FluidEjectionHelper(getOutputHatches(stacks), protectExcess);
        int ejected = ejectionHelper.ejectFluids(stacks, 1, remaining);
        ejectionHelper.commit();

        return ejected == 1;
    }

    @Override
    public @NotNull List<MTEHatchOutput> getValidHatches() {
        return filterValidMTEs(mOutputHatches);
    }

    @Override
    public boolean tryAttachFluidOutput(@NotNull IMetaTileEntity part) {
        if (part instanceof MTEHatchOutput hatch) {
            if (!mOutputHatches.contains(hatch))
            {
                mOutputHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mOutputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }
}
