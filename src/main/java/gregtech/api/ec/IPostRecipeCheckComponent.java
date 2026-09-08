package gregtech.api.ec;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import org.jetbrains.annotations.NotNull;

public interface IPostRecipeCheckComponent extends IComponent {
    @NotNull CheckRecipeResult postCheckRecipe(@NotNull CheckRecipeResult result, @NotNull ProcessingLogic logic);
}
