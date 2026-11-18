package gregtech.common.ec.multiblock;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class RandomMaintenanceIssueComponent extends BaseMultiblockComponent implements IOnRecipeTickComponent {

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (recipeLogicComponent.getRuntime() == 1000) {
            if (getMachine().getRandomNumber(6000) == 0) {
                var maintenanceLogic = getComponent(IMaintenanceLogicComponent.KEY);
                maintenanceLogic.causeRandomIssue();
            }
        }
        return true;
    }

    @Override
    public int getRecipeTickPriority() {
        return RecipeTickPriority.RANDOM_MAINTAIN_ISSUE.priority;
    }
}
