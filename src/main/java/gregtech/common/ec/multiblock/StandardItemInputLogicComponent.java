package gregtech.common.ec.multiblock;

import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IRecipeProcessAwareComponent;
import gregtech.api.ec.ISlotUpdateAwareComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.ec.multiblock.IMultiblockItemInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardItemInputLogicComponent implements IMultiblockItemInputLogicComponent, ISlotUpdateAwareComponent, IRecipeProcessAwareComponent {
    private final ArrayList<MTEHatchInputBus> mInputBusses = new ArrayList<>();

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part) {
        if (part instanceof IDualInputHatch hatch) {
            return false;
        }
        if (part instanceof MTEHatchInputBus hatch) {
            setHatchRecipeMap(hatch);
            if (!mInputBusses.contains(hatch))
            {
                mInputBusses.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches()
    {
        mInputBusses.clear();
    }

    @Override
    public void explodeAllHatches(long power){
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    @Override
    public @NotNull List<MTEHatchInputBus> getValidHatches() {
        return filterValidMTEs(mInputBusses);
    }

    private void setHatchRecipeMap(MTEHatchInputBus hatch)
    {
        if (multiblock != null) {
            hatch.mRecipeMap = multiblock.getComponent(IRecipeLogicComponent.class).getRecipeMap();
        }
    }

    @Override
    public boolean depleteInputAtomic(@NotNull ItemStack aStack, boolean simulate) {
        if (GTUtility.isStackInvalid(aStack)) return false;
        int stackCost = aStack.stackSize;
        searchLoop:
        for (MTEHatchInputBus tHatch : getValidHatches()) {
            setHatchRecipeMap(tHatch);
            final IGregTechTileEntity baseMetaTileEntity = tHatch.getBaseMetaTileEntity();
            for (int i = baseMetaTileEntity.getSizeInventory() - 1; i >= 0; i--) {
                ItemStack stackInSlot = baseMetaTileEntity.getStackInSlot(i);
                if (GTUtility.areStacksEqual(aStack, stackInSlot)) {
                    if (stackInSlot.stackSize >= aStack.stackSize) {
                        if (simulate) return true;
                        baseMetaTileEntity.decrStackSize(i, aStack.stackSize);
                        return true;
                    }
                    stackCost -= stackInSlot.stackSize;
                    if (stackCost <= 0) break searchLoop;
                }
            }
        }
        if (stackCost <= 0)
        {
            if (simulate) return true;
            stackCost = aStack.stackSize;
            for (MTEHatchInputBus tHatch : getValidHatches()) {
                final IGregTechTileEntity baseMetaTileEntity = tHatch.getBaseMetaTileEntity();
                for (int i = baseMetaTileEntity.getSizeInventory() - 1; i >= 0; i--) {
                    ItemStack stackInSlot = baseMetaTileEntity.getStackInSlot(i);
                    if (GTUtility.areStacksEqual(aStack, stackInSlot)) {
                        int amount = Math.min(stackCost, stackInSlot.stackSize);
                        baseMetaTileEntity.decrStackSize(i, amount);
                        stackCost -= amount;
                        if (stackCost == 0) return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void appendStoredItemsForColor(@NotNull ArrayList<ItemStack> rList, @Nullable Byte color) {
        Map<GTUtility.ItemId, ItemStack> inputsFromME = new HashMap<>();
        for (MTEHatchInputBus tHatch : getValidHatches()) {
            if (tHatch instanceof MTEHatchCraftingInputME) {
                continue;
            }
            byte busColor = tHatch.getColor();
            if (color != null && busColor != -1 && busColor != color) continue;
            setHatchRecipeMap(tHatch);
            IGregTechTileEntity tileEntity = tHatch.getBaseMetaTileEntity();
            boolean isMEBus = tHatch instanceof MTEHatchInputBusME;
            for (int i = tileEntity.getSizeInventory() - 1; i >= 0; i--) {
                ItemStack itemStack = tileEntity.getStackInSlot(i);
                if (itemStack != null) {
                    if (isMEBus) {
                        // Prevent the same item from different ME buses from being recognized
                        inputsFromME.put(GTUtility.ItemId.createNoCopy(itemStack), itemStack);
                    } else {
                        rList.add(itemStack);
                    }
                }
            }
        }

        if (!inputsFromME.isEmpty()) {
            rList.addAll(inputsFromME.values());
        }
    }

    @Override
    public void appendStoredItemsFromME(@NotNull Map<GTUtility.ItemId, ItemStack> inputsFromME) {
        for (MTEHatchInputBus tHatch : getValidHatches()) {
            if (tHatch instanceof MTEHatchInputBusME meBus) {
                for (int i = meBus.getSizeInventory() - 1; i >= 0; i--) {
                    ItemStack itemStack = meBus.getStackInSlot(i);
                    if (itemStack != null) {
                        // Prevent the same item from different ME buses from being recognized
                        inputsFromME.put(GTUtility.ItemId.createNoCopy(itemStack), itemStack);
                    }
                }
            }
        }
    }

    @Override
    public void startRecipeProcessing() {
        for (MTEHatchInputBus hatch : getValidHatches()) {
            if (hatch instanceof IRecipeProcessingAwareHatch aware) {
                aware.startRecipeProcessing();
            }
        }
    }

    @Override
    public CheckRecipeResult endRecipeProcessing() {
        CheckRecipeResult worstResult = CheckRecipeResultRegistry.SUCCESSFUL;
        for (MTEHatchInputBus hatch : getValidHatches()) {
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
