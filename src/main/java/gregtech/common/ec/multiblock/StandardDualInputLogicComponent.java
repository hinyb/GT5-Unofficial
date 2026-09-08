package gregtech.common.ec.multiblock;

import gregtech.api.ec.IRecipeCheckHandlerComponent;
import gregtech.api.ec.multiblock.IDualInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static gregtech.api.util.GTUtility.*;

public class StandardDualInputLogicComponent extends BaseMultiblockComponent implements IDualInputLogicComponent, IRecipeCheckHandlerComponent {
    private final List<IDualInputHatch> mDualInputHatches = new ArrayList<>();
    private final BooleanSupplier supportsMEBuffer;

    public StandardDualInputLogicComponent(BooleanSupplier supportsMEBuffer) {
        this.supportsMEBuffer = supportsMEBuffer;
    }

    public @NotNull List<IDualInputHatch> getValidHatches() {
        return uncheckFilterValidMTEs(mDualInputHatches);
    }

    @Override
    public boolean tryAttachDualInput(@NotNull IMetaTileEntity part)
    {
        if (!supportsMEBuffer.getAsBoolean()) return false;
        if (part instanceof IDualInputHatch hatch) {
            if (!mDualInputHatches.contains(hatch))
            {
                mDualInputHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mDualInputHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
    }

    @Override
    public @NotNull CheckRecipeResult tryFindRecipe(@NotNull ProcessingLogic logic, @NotNull CheckRecipeResult lastResult) {
        CheckRecipeResult result = lastResult;
        for (IDualInputHatch dualInputHatch : uncheckedValidMTEList(mDualInputHatches)) {
            ItemStack[] sharedItems = dualInputHatch.getSharedItems();
            for (var it = dualInputHatch.inventories(); it.hasNext();) {
                IDualInputInventory slot = it.next();

                if (!slot.isEmpty()) {
                    // try to cache the possible recipes from pattern
                    if (slot instanceof IDualInputInventoryWithPattern withPattern) {
                        if (!logic.tryCachePossibleRecipesFromPattern(withPattern)) {
                            // move on to next slots if it returns false, which means there is no possible recipes with
                            // given pattern.
                            continue;
                        }
                    }

                    logic.setInputItems(ArrayUtils.addAll(sharedItems, slot.getItemInputs()));
                    logic.setInputFluids(slot.getFluidInputs());

                    CheckRecipeResult foundResult = logic.process();
                    if (foundResult.wasSuccessful()) {
                        return foundResult;
                    }
                    if (foundResult != CheckRecipeResultRegistry.NO_RECIPE) {
                        // Recipe failed in interesting way, so remember that and continue searching
                        result = foundResult;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public @NotNull RecipeCheckPriority getRecipeCheckPriority() {
        return RecipeCheckPriority.DUAL_INPUT;
    }
}
