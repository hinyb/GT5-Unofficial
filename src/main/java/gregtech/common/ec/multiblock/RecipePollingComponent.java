package gregtech.common.ec.multiblock;

import static gregtech.api.ec.component.IRecipeLogicComponent.RECIPE_POLLING;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.multiblock.IBatchConfigComponent;
import gregtech.api.ec.component.multiblock.ISmartHatchLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class RecipePollingComponent extends BaseMultiblockComponent {

    private static final int CHECK_INTERVAL = 100; // How often should we check for a new recipe on an idle machine?
    private final int randomTickOffset = (int) (Math.random() * CHECK_INTERVAL + 1);

    public boolean shouldPollRecipe(long totalRuntime, long timeElapsed) {
        if (smartHatchLogic == null || !smartHatchLogic.needsPeriodicChecks()) {
            return false;
        }
        if (timeElapsed >= CHECK_INTERVAL) {
            return (totalRuntime + randomTickOffset) % CHECK_INTERVAL == 0;
        }
        if (batchConfig != null && batchConfig.isBatchModeEnabled()) {
            return false;
        }
        return timeElapsed == 5 || timeElapsed == 12
            || timeElapsed == 20
            || timeElapsed == 30
            || timeElapsed == 40
            || timeElapsed == 55
            || timeElapsed == 70
            || timeElapsed == 85;
    }

    private @Nullable ISmartHatchLogicComponent smartHatchLogic;
    private @Nullable IBatchConfigComponent batchConfig;

    @Override
    public void onComponentsReady() {
        smartHatchLogic = tryGetComponent(ISmartHatchLogicComponent.KEY);
        batchConfig = tryGetComponent(IBatchConfigComponent.KEY);
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(RECIPE_POLLING, this::shouldPollRecipe);
    }
}
