package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

public interface IProgressIncrementComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IProgressIncrementComponent> KEY = ComponentKey.unique();

    void incrementProgressTime(@NotNull IRecipeLogicComponent recipeLogic);
}
