package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipe.check.CheckRecipeResult;

public interface IRecipeProcessAwareComponent extends IComponent {

    ComponentKey.ListKey<IRecipeProcessAwareComponent> KEY = ComponentKey.list();

    void startRecipeProcessing();

    @NotNull
    CheckRecipeResult endRecipeProcessing();
}
