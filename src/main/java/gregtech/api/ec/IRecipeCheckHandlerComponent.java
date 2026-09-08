package gregtech.api.ec;

import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import org.jetbrains.annotations.NotNull;

public interface IRecipeCheckHandlerComponent extends IComponent {
    enum RecipeCheckPriority {
        DUAL_INPUT,
        CUSTOM_HATCHES,
        STAND_INPUT
    }
    @NotNull CheckRecipeResult tryFindRecipe(@NotNull ProcessingLogic logic, @NotNull CheckRecipeResult lastResult);
    @NotNull RecipeCheckPriority getRecipeCheckPriority();
}
