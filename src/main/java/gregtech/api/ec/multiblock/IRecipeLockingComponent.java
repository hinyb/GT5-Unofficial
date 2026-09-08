package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.recipe.check.SingleRecipeCheck;

public interface IRecipeLockingComponent extends IMultiblockComponent, IUniqueComponent {
    boolean isRecipeLockingEnabled();
    void setRecipeLocking(boolean enabled);
    SingleRecipeCheck getSingleRecipeCheck();
    void setSingleRecipeCheck(SingleRecipeCheck recipeCheck);
}
