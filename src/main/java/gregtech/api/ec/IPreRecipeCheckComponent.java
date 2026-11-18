package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.recipe.check.CheckRecipeResult;

public interface IPreRecipeCheckComponent extends IComponent {

    ComponentKey.ListKey<IPreRecipeCheckComponent> KEY = ComponentKey.list();

    @Nullable
    CheckRecipeResult onPreRecipeCheck(@NotNull IRecipeLogicComponent recipeLogic);
}
