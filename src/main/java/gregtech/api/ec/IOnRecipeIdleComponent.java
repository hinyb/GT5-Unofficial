package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

public interface IOnRecipeIdleComponent extends IComponent {
    enum RecipeIdlePriority {
        EFFICIENCY
    }
    void onRecipeIdle(@NotNull IRecipeLogicComponent recipeLogicComponent);
    @NotNull RecipeIdlePriority getRecipeIdlePriority();
}
