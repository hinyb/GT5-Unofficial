package gregtech.api.ec;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

public interface IOnRecipeTickComponent extends IComponent {

    ComponentKey.ListKey<IOnRecipeTickComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IOnRecipeTickComponent::getRecipeTickPriority));

    enum RecipeTickPriority {

        CONTROLLER_TOOL_WEAR(1000),
        MAINTAIN_CHECK(2000),
        RANDOM_MAINTAIN_ISSUE(3000),
        CONSUME_ENERGY(4000),
        POLLUTE(5000);

        public final int priority;

        RecipeTickPriority(int priority) {
            this.priority = priority;
        }
    }

    boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent);

    int getRecipeTickPriority();
}
