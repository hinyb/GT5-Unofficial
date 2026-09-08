package gregtech.api.ec;

import gregtech.api.recipe.check.CheckRecipeResult;

public interface IRecipeProcessAwareComponent extends IComponent {
    void startRecipeProcessing();
    CheckRecipeResult endRecipeProcessing();
}
