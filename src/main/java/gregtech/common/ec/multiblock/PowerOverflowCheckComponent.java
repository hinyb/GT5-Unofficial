package gregtech.common.ec.multiblock;

import gregtech.api.ec.IPostRecipeCheckComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;

public class PowerOverflowCheckComponent extends BaseMultiblockComponent implements IPostRecipeCheckComponent {
    @Override
    public @NotNull CheckRecipeResult postCheckRecipe(@NotNull CheckRecipeResult result, @NotNull ProcessingLogic logic) {
        if (result.wasSuccessful() && logic.getCalculatedEut() > Integer.MAX_VALUE) {
            return CheckRecipeResultRegistry.POWER_OVERFLOW;
        }
        return result;
    }
}
