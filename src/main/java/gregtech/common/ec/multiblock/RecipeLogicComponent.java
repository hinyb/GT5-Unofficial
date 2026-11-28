package gregtech.common.ec.multiblock;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.Signal;
import gregtech.api.ec.component.IProcessingLogicRecipeComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.ec.multiblock.internal.BaseRecipeLogicComponent;

public class RecipeLogicComponent extends BaseRecipeLogicComponent implements IProcessingLogicRecipeComponent {

    @Override
    public @NotNull ProcessingLogic getProcessingLogic() {
        return processingLogic;
    }

    @FunctionalInterface
    public interface IRecipeMapProvider {

        @Nullable
        RecipeMap<?> getRecipeMap();
    }

    protected @NotNull final ProcessingLogic processingLogic;
    protected @NotNull final IRecipeMapProvider recipeMapProvider;
    private boolean isGenerator = false;

    public RecipeLogicComponent setGenerator(boolean isGenerator) {
        this.isGenerator = isGenerator;
        return this;
    }

    public RecipeLogicComponent(@NotNull ProcessingLogic processingLogic,
        @NotNull IRecipeMapProvider recipeMapProvider) {
        this.processingLogic = processingLogic;
        this.recipeMapProvider = recipeMapProvider;
    }

    @Override
    public @Nullable RecipeMap<?> getRecipeMap() {
        return recipeMapProvider.getRecipeMap();
    }

    @Override
    public long getMaxAllowedRecipeEUt() {
        setupProcessingLogic(processingLogic);
        return processingLogic.getMaxAllowedRecipeEUt();
    }

    @Override
    public int getCurrentParallels() {
        return processingLogic.getCurrentParallels();
    }

    protected void setupProcessingLogic(ProcessingLogic logic) {
        logic.clear();
        logic.setMachine(getMachine());
        logic.setRecipeMapSupplier(this::getRecipeMap);
        for (var listener : processingLogicSetups.listeners()) {
            listener.setupProcessingLogic(logic);
        }
    }

    /**
     * Iterates over hatches and tries to find recipe. Assume {@link #processingLogic} is already set up for use. If
     * return value is successful, inputs are consumed.
     */
    @Nonnull
    protected CheckRecipeResult doCheckRecipe() {
        CheckRecipeResult result = CheckRecipeResultRegistry.NO_RECIPE;

        for (var listener : recipeCheckHandlers.listeners()) {
            result = listener.tryFindRecipe(processingLogic, result);
            if (result.wasSuccessful()) return result;
        }

        return result;
    }

    /**
     * Performs additional check for {@link #processingLogic} after all the calculations are done. As many as checks
     * should be done inside of custom {@link ProcessingLogic}, because when this method is called, inputs might have
     * been already consumed.
     * However, certain checks cannot be done like that; Checking energy overflow should be suppressed for long-power
     * machines for example.
     *
     * @return Modified (or not modified) result
     */
    @Nonnull
    protected CheckRecipeResult postCheckRecipe(@NotNull CheckRecipeResult result,
        @Nonnull ProcessingLogic processingLogic) {
        for (var listeners : postRecipeChecks.listeners()) {
            result = listeners.onPostRecipeCheck(result, processingLogic);
        }
        return result;
    }

    protected void setEnergyUsage(ProcessingLogic processingLogic) {
        long eut = processingLogic.getCalculatedEut();
        mEUt = isGenerator ? Math.abs(eut) : -Math.abs(eut);
    }

    /**
     * Checks recipe and setup machine if it's successful.
     * <p>
     * For generic machine working with recipemap.
     */
    @Nonnull
    public CheckRecipeResult checkProcessing() {
        for (var listener : preRecipeChecks.listeners()) {
            var result = listener.onPreRecipeCheck(this);
            if (result != null) {
                updateSlots();
                return result;
            }
        }
        setupProcessingLogic(processingLogic);

        CheckRecipeResult result = doCheckRecipe();
        result = postCheckRecipe(result, processingLogic);
        // inputs are consumed at this point
        updateSlots();
        if (!result.wasSuccessful()) return result;

        mMaxProgresstime = processingLogic.getDuration();
        setEnergyUsage(processingLogic);
        mOutputItems = processingLogic.getOutputItems();
        mOutputFluids = processingLogic.getOutputFluids();
        return result;
    }

    private Signal<IPreRecipeCheck> preRecipeChecks;
    private Signal<IRecipeCheckHandler> recipeCheckHandlers;
    private Signal<IProcessingLogicSetup> processingLogicSetups;
    private Signal<IPostRecipeCheck> postRecipeChecks;

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        preRecipeChecks = signal(PRE_RECIPE_CHECK);
        recipeCheckHandlers = signal(RECIPE_CHECK_HANDLER);
        processingLogicSetups = signal(PROCESSING_LOGIC_SETUP);
        postRecipeChecks = signal(POST_RECIPE_CHECK);
    }
}
