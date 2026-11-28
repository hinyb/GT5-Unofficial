package gregtech.common.ec.multiblock;

import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_TICK;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.multiblock.IControllerToolWearComponent;
import gregtech.common.config.MachineStats;
import gregtech.common.items.MetaGeneratedTool01;

public class ControllerToolWearComponent extends ControllerPartComponent implements IControllerToolWearComponent {

    private final int damageFactorLow;
    private final float damageFactorHigh;

    @Override
    public long calculateDamageModifier(long eut) {
        return (long) Math.min((double) eut / damageFactorLow, Math.pow(eut, damageFactorHigh));
    }

    @Override
    public long getExpectedDamage(@Nullable ItemStack tool, long eut) {
        if (tool == null) return 0;
        long modifier = calculateDamageModifier(eut);
        return damageCalculator.getDamageToComponent(tool) * modifier;
    }

    @FunctionalInterface
    public interface IToolDamageCalculator {

        /**
         * Gets the factor value to damage the ItemStack in the controller slot.
         * <p>
         * This function will be called only if the ItemStack is {@link MetaGeneratedTool01}, and the actual applied
         * damage
         * value is multiplied by
         * <a href="https://www.wolframalpha.com/input?i=plot+min%28x%2F5%2C+x%5E%280.6%29%29+range+0+to+512">a
         * formula</a>
         * calculated with {@link #damageFactorLow} and {@link #damageFactorHigh}.
         *
         * @param aStack the ItemStack in the controller slot.
         * @return the factor value to damage the ItemStack in the controller slot.
         */
        @Range(from = 0, to = Integer.MAX_VALUE)
        int getDamageToComponent(@Nullable ItemStack aStack);
    }

    private final IToolDamageCalculator damageCalculator;

    public ControllerToolWearComponent(IToolValidator validator, IToolDamageCalculator damageCalculator) {
        super(validator);
        this.damageCalculator = damageCalculator;
        this.damageFactorLow = MachineStats.machines.damageFactorLow;
        this.damageFactorHigh = MachineStats.machines.damageFactorHigh;
    }

    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        var machine = getMachine();
        var controllerTool = machine.getControllerSlot();
        if (recipeLogicComponent.getRuntime() == 1000) {
            if (controllerTool != null && machine.getRandomNumber(2) == 0
                && !controllerTool.getUnlocalizedName()
                    .startsWith("gt.blockmachines.basicmachine.")) {
                if (controllerTool.getItem() instanceof MetaGeneratedTool01 metaGeneratedTool) {
                    long EUt = Math.abs(recipeLogicComponent.getEUt());
                    metaGeneratedTool.doDamage(controllerTool, getExpectedDamage(controllerTool, EUt));
                    if (controllerTool.stackSize == 0) machine.setControllerSlot(null);
                }
            }
        }
        return true;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_RECIPE_TICK, IRecipeLogicComponent.IOnRecipeTick.CONTROLLER_PART_WEAR, this::onRecipeTick);
    }
}
