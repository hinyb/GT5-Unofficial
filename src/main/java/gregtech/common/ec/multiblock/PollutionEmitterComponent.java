package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;

public class PollutionEmitterComponent extends BaseMultiblockComponent implements IOnRecipeTickComponent {
    @FunctionalInterface
    public interface IPollutionCalculator {
        /**
         * Gets the pollution produced per second by this multiblock, default to 0. Override this with its actual value in
         * the code of the multiblock.
         *
         * This returns the unmodified raw pollution value, not the one after muffler discounts.
         *
         * @param aStack what is in controller
         */
        @Range(from = 0, to = Integer.MAX_VALUE)
        int getDamageToComponent(@Nullable ItemStack aStack);
    }
    private final IPollutionCalculator pollutionCalculator;

    public PollutionEmitterComponent(IPollutionCalculator pollutionCalculator) {
        this.pollutionCalculator = pollutionCalculator;
    }

    /**
     * Gets the pollution this Device outputs to a Muffler per tick (10000 = one Pullution Block)
     *
     * @param aStack what is in controller
     */
    public int getPollutionPerTick(ItemStack aStack) {
        return pollutionCalculator.getDamageToComponent(aStack) / 20;
    }

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (!pollutionLogic.polluteEnvironment(getPollutionPerTick(getMachine().getControllerSlot()))) {
            stopMachine(ShutDownReasonRegistry.POLLUTION_FAIL);
        }
        return true;
    }

    @Override
    public @NotNull RecipeTickPriority getRecipeTickPriority() {
        return RecipeTickPriority.POLLUTE;
    }

    private IPollutionLogicComponent pollutionLogic;
    @Override
    public void onComponentsReady(){
        pollutionLogic = getComponent(IPollutionLogicComponent.class);
    }
}
