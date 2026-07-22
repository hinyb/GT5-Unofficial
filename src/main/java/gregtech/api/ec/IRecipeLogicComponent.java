package gregtech.api.ec;

import gregtech.api.recipe.RecipeMap;

public interface IRecipeLogicComponent extends IComponent {
    RecipeMap<?> getRecipeMap();
}
