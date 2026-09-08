package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

public interface IOnRecipeStartComponent extends IComponent {
    enum RecipeStartPriority {
        EFFICIENCY,
        SOUND
    }
    void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent);
    @NotNull RecipeStartPriority getRecipeStartPriority();
}
