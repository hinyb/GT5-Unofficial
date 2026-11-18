package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;

public interface IPostRecipeCheckComponent extends IComponent {

    ComponentKey.ListKey<IPostRecipeCheckComponent> KEY = ComponentKey.list();

    @NotNull
    CheckRecipeResult postCheckRecipe(@NotNull CheckRecipeResult result, @NotNull ProcessingLogic logic);
}
