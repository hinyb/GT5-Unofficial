package gregtech.common.ec.multiblock;

import gregtech.api.ec.IFluidInputLogicComponent;
import gregtech.api.ec.IItemInputLogicComponent;
import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.ec.IRecipeCheckHandlerComponent;
import gregtech.api.ec.multiblock.IInputSeparationComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockItemInputLogicComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTESteamMultiBlockBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandardColorRecipeCheckComponent extends BaseMultiblockComponent implements IRecipeCheckHandlerComponent {
    private boolean canUseControllerSlotForRecipe = true;

    public StandardColorRecipeCheckComponent setCanUseControllerSlotForRecip(boolean canUseControllerSlotForRecipe)
    {
        this.canUseControllerSlotForRecipe = canUseControllerSlotForRecipe;
        return this;
    }

    private short getHatchColors() {
        short hatchColors = 0;

        if (itemInputLogic != null) hatchColors |= itemInputLogic.getHatchColors();
        if (fluidInputLogic != null) hatchColors |= fluidInputLogic.getHatchColors();

        if (getMachine() instanceof MTESteamMultiBlockBase<?> steamMultiBase) {
            for (var bus : steamMultiBase.mSteamInputs) hatchColors |= (short) (1 << bus.getColor());
            for (var hatch : steamMultiBase.mSteamInputFluids) hatchColors |= (short) (1 << hatch.getColor());
        }

        return hatchColors;
    }

    /**
     * Returns whether the given color is absent in the hatch color bitmask.
     *
     * @param hatchColors bitmask of present colors (one bit per color index).
     * @param color       color index to check (0–15).
     * @return {@code true} if the color is absent, {@code false} if present.
     */
    private static boolean isColorAbsent(short hatchColors, byte color) {
        return (hatchColors & (1 << color)) == 0;
    }

    @Override
    public @NotNull CheckRecipeResult tryFindRecipe(@NotNull ProcessingLogic logic, @NotNull CheckRecipeResult lastResult) {
        CheckRecipeResult result = lastResult;
        // Use hatch colors if any; fallback to color 1 otherwise.
        short hatchColors = getHatchColors();
        boolean doColorChecking = hatchColors != 0;
        if (!doColorChecking) hatchColors = 0b1;

        boolean isInputSeparationEnabled = inputSeparation != null && inputSeparation.isInputSeparationEnabled();

        List<FluidStack> fluidBuffer = new ArrayList<>();
        List<ItemStack> itemBuffer = new ArrayList<>();

        ItemStack controllerSlotItem = canUseControllerSlotForRecipe ? getMachine().getControllerSlot() : null;
        for (byte color = 0; color < (doColorChecking ? 16 : 1); color++) {
            if (isColorAbsent(hatchColors, color)) continue;
            if (fluidInputLogic != null)
            {
                fluidBuffer.clear();
                fluidInputLogic.appendStoredFluidsForColor(fluidBuffer, color);
            }
            logic.setInputFluids(fluidBuffer);
            if (isInputSeparationEnabled) {
                List<MTEHatchInputBus> validBusses = itemInputLogic != null ? itemInputLogic.getValidHatches() : Collections.emptyList();
                if (validBusses.isEmpty()) {
                    itemBuffer.clear();
                    if (controllerSlotItem != null) itemBuffer.add(controllerSlotItem);
                    logic.setInputItems(itemBuffer);
                    CheckRecipeResult foundResult = logic.process();
                    if (foundResult.wasSuccessful()) return foundResult;
                    if (foundResult != CheckRecipeResultRegistry.NO_RECIPE) result = foundResult;
                } else {
                    for (MTEHatchInputBus bus : validBusses)
                    {
                        if (bus instanceof MTEHatchCraftingInputME) continue;
                        byte busColor = bus.getColor();
                        if (busColor != -1 && busColor != color) continue;
                        itemBuffer.clear();
                        for (int i = bus.getSizeInventory() - 1; i >= 0; i--) {
                            ItemStack stored = bus.getStackInSlot(i);
                            if (stored != null) itemBuffer.add(stored);
                        }
                        if (controllerSlotItem != null) itemBuffer.add(controllerSlotItem);
                        logic.setInputItems(itemBuffer);
                        CheckRecipeResult foundResult = logic.process();
                        if (foundResult.wasSuccessful()) return foundResult;
                        // Recipe failed in interesting way, so remember that and continue searching
                        if (foundResult != CheckRecipeResultRegistry.NO_RECIPE) result = foundResult;
                    }
                }
            } else {
                if (itemInputLogic != null) {
                    itemBuffer.clear();
                    itemInputLogic.appendStoredItemsForColor(itemBuffer, color);
                }
                if (controllerSlotItem != null) itemBuffer.add(controllerSlotItem);
                logic.setInputItems(itemBuffer);
                CheckRecipeResult foundResult = logic.process();
                if (foundResult.wasSuccessful()) return foundResult;
                // Recipe failed in interesting way, so remember that
                if (foundResult != CheckRecipeResultRegistry.NO_RECIPE) result = foundResult;
            }
        }
        return result;
    }

    @Override
    public @NotNull RecipeCheckPriority getRecipeCheckPriority() {
        return RecipeCheckPriority.STAND_INPUT;
    }

    private @Nullable IMultiblockItemInputLogicComponent itemInputLogic;
    private @Nullable IMultiblockFluidInputLogicComponent fluidInputLogic;
    private @Nullable IInputSeparationComponent inputSeparation;
    @Override
    public void onComponentsReady(){
        itemInputLogic = tryGetComponent(IMultiblockItemInputLogicComponent.class);
        fluidInputLogic = tryGetComponent(IMultiblockFluidInputLogicComponent.class);
        inputSeparation = tryGetComponent(IInputSeparationComponent.class);
    }
}
