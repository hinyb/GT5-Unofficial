package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface IOnRecipeFinishComponent extends IComponent {
    enum RecipeFinishPriority {
        DRONE_COLLECT_DATA,
        EFFICIENCY
    }
    void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent);
    @NotNull RecipeFinishPriority getRecipeFinishPriority();
}
