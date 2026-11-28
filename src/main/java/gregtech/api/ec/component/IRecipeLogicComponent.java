package gregtech.api.ec.component;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.SignalKey;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public interface IRecipeLogicComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IRecipeLogicComponent> KEY = ComponentKey.unique();

    @Nullable
    RecipeMap<?> getRecipeMap();

    long getEUt();

    void setEUt(long eut);

    int getProgressTime();

    void setProgressTime(int progressTime);

    int getMaxProgressTime();

    void setMaxProgressTime(int maxProgressTime);

    int getRuntime();

    long getLastWorkingTick();

    @NotNull
    CheckRecipeResult getCheckRecipeResult();

    void setCheckRecipeResult(@NotNull CheckRecipeResult result);

    void scheduleRecipeCheck(RecipeCheckReason reason);

    @Nullable
    ItemStack[] getOutputItems();

    void setOutputItems(@Nullable ItemStack[] outputItems);

    @Nullable
    FluidStack[] getOutputFluids();

    void setOutputFluids(@Nullable FluidStack[] outputFluids);

    boolean addPendingOutputs(@NotNull ItemStack[] outputItems);

    boolean addPendingOutputs(@NotNull FluidStack[] outputFluids);

    boolean hasPendingOutputs();

    long getMaxAllowedRecipeEUt();

    long getRecipesDone();

    boolean wasRunningOnLoad();

    void clearRunningOnLoad();

    @FunctionalInterface
    interface IOnRecipeFinish {

        void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent);

        int DRONE_COLLECT_DATA = -2000;
        int EFFICIENCY = -1000;
    }

    SignalKey<IOnRecipeFinish> ON_RECIPE_FINISH = SignalKey.create();

    @FunctionalInterface
    interface IOnRecipeIdle {

        void onRecipeIdle(@NotNull IRecipeLogicComponent recipeLogicComponent);

        int EFFICIENCY = -1000;
    }

    SignalKey<IOnRecipeIdle> ON_RECIPE_IDLE = SignalKey.create();

    @FunctionalInterface
    interface IOnRecipeStart {

        void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent);

        int EFFICIENCY = -2000;
        int SOUND = -1000;
    }

    SignalKey<IOnRecipeStart> ON_RECIPE_START = SignalKey.create();

    @FunctionalInterface
    interface IOnRecipeTick {

        boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent);

        int CONTROLLER_PART_WEAR = -5000;
        int CONTROLLER_PART_CHECK = -4000;
        int RANDOM_MAINTAIN_ISSUE = -3000;
        int CONSUME_ENERGY = -2000;
        int POLLUTE = -1000;
    }

    SignalKey<IOnRecipeTick> ON_RECIPE_TICK = SignalKey.create();

    @FunctionalInterface
    interface IPreRunMachine {

        boolean onPreRunMachine(@NotNull IRecipeLogicComponent recipeLogic, IGregTechTileEntity igte, long tick);
    }

    SignalKey<IPreRunMachine> PRE_RUN_MACHINE = SignalKey.create();

    @FunctionalInterface
    interface IPostRunMachine {

        void onPostRunMachine(@NotNull IRecipeLogicComponent recipeLogic, IGregTechTileEntity igte, long tick);
    }

    SignalKey<IPostRunMachine> POST_RUN_MACHINE = SignalKey.create();

    @FunctionalInterface
    interface IOnSlotUpdate {

        void updateSlots();
    }

    SignalKey<IOnSlotUpdate> ON_SLOT_UPDATE = SignalKey.create();

    @FunctionalInterface
    interface IStartRecipeProcessing {

        void startRecipeProcessing();
    }

    SignalKey<IStartRecipeProcessing> START_RECIPE_PROCESSING = SignalKey.create();

    @FunctionalInterface
    interface IEndRecipeProcessing {

        @NotNull
        CheckRecipeResult endRecipeProcessing();
    }

    SignalKey<IEndRecipeProcessing> END_RECIPE_PROCESSING = SignalKey.create();

    @FunctionalInterface
    interface IRecipePolling {

        boolean shouldPollRecipe(long totalRuntime, long timeElapsed);
    }

    SignalKey<IRecipePolling> RECIPE_POLLING = SignalKey.create();
}
