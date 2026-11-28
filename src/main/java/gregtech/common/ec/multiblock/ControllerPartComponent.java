package gregtech.common.ec.multiblock;

import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_TICK;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.multiblock.IControllerPartComponent;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class ControllerPartComponent extends BaseMultiblockComponent implements IControllerPartComponent {

    public ControllerPartComponent(IToolValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return validator.isCorrectMachinePart(aStack);
    }

    @FunctionalInterface
    public interface IToolValidator {

        /**
         * Check the ItemStack in the controller slot.
         *
         * @param aStack the ItemStack in the controller slot.
         * @return {@code true} if the item is valid for this machine or the machine doesn't have restrictions on the
         *         controller slot.
         */
        boolean isCorrectMachinePart(ItemStack aStack);
    }

    private final IToolValidator validator;

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_RECIPE_TICK, IRecipeLogicComponent.IOnRecipeTick.CONTROLLER_PART_CHECK, this::onRecipeTick);
    }

    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (!isCorrectMachinePart(getControllerSlot())) {
            stopMachine(ShutDownReasonRegistry.NO_MACHINE_PART);
            return false;
        }
        return true;
    }
}
