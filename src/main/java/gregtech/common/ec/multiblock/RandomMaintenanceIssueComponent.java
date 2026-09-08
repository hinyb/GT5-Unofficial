package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;

public class RandomMaintenanceIssueComponent extends BaseMultiblockComponent implements IOnRecipeTickComponent {
    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (recipeLogicComponent.getRuntime() == 1000) {
            if (getMachine().getRandomNumber(6000) == 0) {
                maintenanceLogic.causeRandomIssue();
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeTickPriority getRecipeTickPriority() {
        return RecipeTickPriority.MAINTAIN_ISSUE;
    }

    private IMaintenanceLogicComponent maintenanceLogic;

    @Override
    public void onComponentsReady(){
        maintenanceLogic = getComponent(IMaintenanceLogicComponent.class);
    }
}
