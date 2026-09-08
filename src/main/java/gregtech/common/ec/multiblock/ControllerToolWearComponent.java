package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.config.MachineStats;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.items.MetaGeneratedTool01;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;

public class ControllerToolWearComponent extends BaseMultiblockComponent implements IOnRecipeTickComponent {
    private final int damageFactorLow;
    private final float damageFactorHigh;
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
    @FunctionalInterface
    public interface IToolDamageCalculator {
        /**
         * Gets the factor value to damage the ItemStack in the controller slot.
         * <p>
         * This function will be called only if the ItemStack is {@link MetaGeneratedTool01}, and the actual applied damage
         * value is multiplied by
         * <a href="https://www.wolframalpha.com/input?i=plot+min%28x%2F5%2C+x%5E%280.6%29%29+range+0+to+512">a formula</a>
         * calculated with {@link #damageFactorLow} and {@link #damageFactorHigh}.
         *
         * @param aStack the ItemStack in the controller slot.
         * @return the factor value to damage the ItemStack in the controller slot.
         */
        @Range(from = 0, to = Integer.MAX_VALUE)
        int getDamageToComponent(@Nullable ItemStack aStack);
    }
    private final IToolDamageCalculator damageCalculator;
    ControllerToolWearComponent(IToolValidator validator, IToolDamageCalculator damageCalculator)
    {
        this.validator = validator;
        this.damageCalculator = damageCalculator;
        this.damageFactorLow = MachineStats.machines.damageFactorLow;
        this.damageFactorHigh = MachineStats.machines.damageFactorHigh;
    }

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        var machine = getMachine();
        var controllerTool = machine.getControllerSlot();
        if (!validator.isCorrectMachinePart(controllerTool)) {
            stopMachine(ShutDownReasonRegistry.NO_MACHINE_PART);
            return false;
        }
        if (recipeLogicComponent.getRuntime() == 1000) {
            if (controllerTool != null && machine.getRandomNumber(2) == 0
                && !controllerTool.getUnlocalizedName()
                .startsWith("gt.blockmachines.basicmachine.")) {
                if (controllerTool.getItem() instanceof MetaGeneratedTool01 metaGeneratedTool) {
                    long EUt = Math.abs(recipeLogicComponent.getEUt());
                    metaGeneratedTool.doDamage(controllerTool,(long) damageCalculator.getDamageToComponent(controllerTool) * (long) Math.min(EUt / damageFactorLow, Math.pow(EUt, damageFactorHigh)));
                    if (controllerTool.stackSize == 0) machine.setControllerSlot(null);
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeTickPriority getRecipeTickPriority() {
        return RecipeTickPriority.CONTROLLER_TOOL_WEAR;
    }
}
