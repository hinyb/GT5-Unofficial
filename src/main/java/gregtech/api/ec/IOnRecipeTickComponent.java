package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

public interface IOnRecipeTickComponent extends IComponent {
    enum RecipeTickPriority {
        CONTROLLER_TOOL_WEAR,
        MAINTAIN_ISSUE,
        CONSUME_ENERGY,
        POLLUTE
    }
    boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent);
    @NotNull RecipeTickPriority getRecipeTickPriority();
}
