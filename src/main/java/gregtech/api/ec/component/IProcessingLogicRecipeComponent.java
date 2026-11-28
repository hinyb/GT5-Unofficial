package gregtech.api.ec.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.SignalKey;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;

public interface IProcessingLogicRecipeComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IProcessingLogicRecipeComponent> KEY = ComponentKey.unique();

    @NotNull
    ProcessingLogic getProcessingLogic();

    @FunctionalInterface
    interface IRecipeCheckHandler {

        @NotNull
        CheckRecipeResult tryFindRecipe(@NotNull ProcessingLogic logic, @NotNull CheckRecipeResult lastResult);

        int DUAL_INPUT = -3000;
        int CUSTOM_HATCHES = -2000;
        int STANDARD_INPUT = -1000;
    }

    SignalKey<IRecipeCheckHandler> RECIPE_CHECK_HANDLER = SignalKey.create();

    @FunctionalInterface
    interface IPreRecipeCheck {

        @Nullable
        CheckRecipeResult onPreRecipeCheck(@NotNull IRecipeLogicComponent recipeLogic);
    }

    SignalKey<IPreRecipeCheck> PRE_RECIPE_CHECK = SignalKey.create();

    @FunctionalInterface
    interface IPostRecipeCheck {

        @NotNull
        CheckRecipeResult onPostRecipeCheck(@NotNull CheckRecipeResult result, @NotNull ProcessingLogic logic);
    }

    SignalKey<IPostRecipeCheck> POST_RECIPE_CHECK = SignalKey.create();

    @FunctionalInterface
    interface IProcessingLogicSetup {

        void setupProcessingLogic(ProcessingLogic logic);
    }

    SignalKey<IProcessingLogicSetup> PROCESSING_LOGIC_SETUP = SignalKey.create();
}
