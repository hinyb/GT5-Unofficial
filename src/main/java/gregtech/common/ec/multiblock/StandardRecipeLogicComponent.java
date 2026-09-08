package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeFinishComponent;
import gregtech.api.ec.IOnRecipeIdleComponent;
import gregtech.api.ec.IOnRecipeStartComponent;
import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IPostRecipeCheckComponent;
import gregtech.api.ec.IRecipeCheckHandlerComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IRecipePollingComponent;
import gregtech.api.ec.IRecipeProcessAwareComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IServerTickableComponent;
import gregtech.api.ec.ISlotUpdateAwareComponent;
import gregtech.api.ec.multiblock.IBatchConfigComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidOutputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockItemOutputLogicComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.ec.multiblock.ISmartHatchLogicComponent;
import gregtech.api.ec.multiblock.IStructureLogicComponent;
import gregtech.api.ec.multiblock.IVoidingConfigComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IVoidable;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.FluidEjectionHelper;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemEjectionHelper;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.config.MachineStats;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.RecipeCheckReason;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class StandardRecipeLogicComponent extends BaseMultiblockComponent implements IRecipeLogicComponent, ISavableComponent, IServerTickableComponent {
    protected int mProgresstime = 0, mMaxProgresstime = 0, mRuntime = 0, lastParallel = 0;
    protected long mEUt = 0;
    /**
     * A pending IMMEDIATE recipe-check push (new inputs, drained output, user/structure change); never throttled.
     */
    protected boolean recipeCheckImmediately = false;
    /**
     * A pending THROTTLED recipe-check push (ME restock, power trickle); deferred while the fail cooldown is active.
     */
    protected boolean recipeCheckThrottled = false;
    /**
     * While {@code mTotalRunTime < this}, THROTTLED rechecks are deferred (the push flag is kept until it expires).
     * Set after a failed check to {@code now + MachineStats.machines.recipeCheckFailCooldown}; 0 means no cooldown.
     * IMMEDIATE pushes ignore this entirely.
     */
    protected long recipeCheckCooldownUntil = 0;

    protected @NotNull CheckRecipeResult checkRecipeResult = CheckRecipeResultRegistry.NONE;

    protected long mLastWorkingTick = 0;

    protected long recipesDone = 0;

    public ItemStack[] mOutputItems = null;
    public FluidStack[] mOutputFluids = null;
    public List<ItemStack> mPendingItems = new ArrayList<>();
    public List<FluidStack> mPendingFluids = new ArrayList<>();

    protected @NotNull final ProcessingLogic processingLogic;
    protected @NotNull final IRecipeMapProvider recipeMapProvider;
    @FunctionalInterface
    public interface IRecipeMapProvider {
        @NotNull RecipeMap<?> getRecipeMap();
    }

    public StandardRecipeLogicComponent(@NotNull ProcessingLogic processingLogic, @NotNull IRecipeMapProvider recipeMapProvider) {
        this.processingLogic = processingLogic;
        this.recipeMapProvider = recipeMapProvider;
    }

    @Override
    public @NotNull RecipeMap<?> getRecipeMap() {
        return recipeMapProvider.getRecipeMap();
    }

    @Override
    public long getEUt() {
        return mEUt;
    }

    @Override
    public int getProgressTime() {
        return mProgresstime;
    }

    @Override
    public int getMaxProgressTime() {
        return mMaxProgresstime;
    }

    @Override
    public int getRuntime() {
        return mRuntime;
    }

    @Override
    public void scheduleRecipeCheck(RecipeCheckReason reason) {
        if (reason.throttled) {
            recipeCheckThrottled = true;
        } else {
            recipeCheckImmediately = true;
        }
    }

    @Override
    public @Nullable ItemStack[] getOutputItems() {
        return mOutputItems;
    }

    @Override
    public @Nullable FluidStack[] getOutputFluids() {
        return mOutputFluids;
    }

    @Override
    public @NotNull CheckRecipeResult getCheckRecipeResult() {
        return checkRecipeResult;
    }

    @Override
    public void setCheckRecipeResult(@NotNull CheckRecipeResult result) {
        this.checkRecipeResult = result;
    }

    protected void incrementProgressTime() {
        mProgresstime++;
    }

    protected void outputAfterRecipe() {

    }

    protected boolean protectsExcessItem() {
        if (voidingConfig == null) return false;
        return voidingConfig.protectsExcessItem();
    }

    protected boolean protectsExcessFluid() {
        if (voidingConfig == null) return false;
        return voidingConfig.protectsExcessFluid();
    }

    protected boolean addPendingOutputs(@NotNull ItemStack[] outputItems) {
        var remaining = protectsExcessItem() ? mPendingItems : null;
        return addItemOutputs(Arrays.asList(outputItems), remaining);
    }

    protected boolean addItemOutputs(@NotNull List<ItemStack> outputItems, @Nullable List<ItemStack> remaining) {
        if (outputItems.isEmpty()) return true;
        if (itemOutputLogic == null) {
            if (remaining != null) {
                remaining.addAll(outputItems);
            }
            return false;
        }
        ItemEjectionHelper ejectionHelper = new ItemEjectionHelper(itemOutputLogic.getOutputBusses(), protectsExcessItem());
        int ejected = ejectionHelper.ejectItems(outputItems, 1, remaining);
        ejectionHelper.commit();

        return ejected == 1;
    }

    protected boolean addPendingOutputs(@NotNull FluidStack[] outputFluids) {
        var remaining = protectsExcessFluid() ? mPendingFluids : null;
        return addFluidOutputs(Arrays.asList(outputFluids), remaining);
    }

    protected boolean addFluidOutputs(@NotNull List<FluidStack> outputFluids, @Nullable List<FluidStack> remaining) {
        if (outputFluids.isEmpty()) return true;
        if (fluidOutputLogic == null) {
            if (remaining != null) {
                remaining.addAll(outputFluids);
            }
            return false;
        }
        FluidEjectionHelper ejectionHelper = new FluidEjectionHelper(fluidOutputLogic.getOutputHatches(outputFluids), protectsExcessFluid());
        int ejected = ejectionHelper.ejectFluids(outputFluids, 1, remaining);
        ejectionHelper.commit();

        return ejected == 1;
    }

    protected boolean tryFlushPendingOutputs() {
        boolean hasChanged = false;
        if (!mPendingItems.isEmpty()) {
            List<ItemStack> pending = new ArrayList<>();
            addItemOutputs(mPendingItems, pending);
            mPendingItems = pending;
            hasChanged = true;
        }
        if (!mPendingFluids.isEmpty()) {
            GTUtility.removeTailingNulls(mPendingFluids);
            if (!mPendingFluids.isEmpty()) {
                List<FluidStack> pending = new ArrayList<>();
                addFluidOutputs(mPendingFluids, pending);
                mPendingFluids = pending;
                hasChanged = true;
            }
        }
        if (hasChanged) markDirty();
        return mPendingItems.isEmpty() && mPendingFluids.isEmpty();
    }

    public void startRecipeProcessing() {
        for (IRecipeProcessAwareComponent component : recipeProcessAwares) {
            component.startRecipeProcessing();
        }
    }

    public void endRecipeProcessing() {
        CheckRecipeResult worstResult = CheckRecipeResultRegistry.SUCCESSFUL;
        for (IRecipeProcessAwareComponent component : recipeProcessAwares) {
            var result = component.endRecipeProcessing();
            if (!result.wasSuccessful()) {
                worstResult = result;
                stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
            }
        }
        setResultIfFailure(worstResult);
    }

    public void setResultIfFailure(CheckRecipeResult result) {
        if (!result.wasSuccessful()) {
            this.checkRecipeResult = result;
        }
    }

    protected void setupProcessingLogic(ProcessingLogic logic) {
        logic.clear();
        // todo refactor
        logic.setMachine((IVoidable) getMachine());
        logic.setRecipeMapSupplier(this::getRecipeMap);
        for (var component : processingLogicSetups) {
            component.setupProcessingLogic(logic);
        }
    }

    /**
     * Override to perform additional checkRecipe logic. It gets called after CRIBs and before ordinary hatches.
     *
     * @param lastResult Last result of checkRecipe. It might contain interesting info about failure, so don't blindly
     *                   overwrite it. Refer to {@link #doCheckRecipe} for how to handle it.
     * @return Result of the checkRecipe.
     */
    @Nonnull
    protected CheckRecipeResult checkRecipeForCustomHatches(CheckRecipeResult lastResult) {
        return lastResult;
    }

    /**
     * Iterates over hatches and tries to find recipe. Assume {@link #processingLogic} is already set up for use. If
     * return value is successful, inputs are consumed.
     */
    @Nonnull
    protected CheckRecipeResult doCheckRecipe() {
        CheckRecipeResult result = CheckRecipeResultRegistry.NO_RECIPE;

        for (var component : recipeCheckHandlers) {
            result = component.tryFindRecipe(processingLogic, result);
            if (result.wasSuccessful()) return result;
        }

        return result;
    }

    /**
     * Performs additional check for {@link #processingLogic} after all the calculations are done. As many as checks
     * should be done inside of custom {@link ProcessingLogic}, because when this method is called, inputs might have been already consumed.
     * However, certain checks cannot be done like that; Checking energy overflow should be suppressed for long-power
     * machines for example.
     *
     * @return Modified (or not modified) result
     */
    @Nonnull
    protected CheckRecipeResult postCheckRecipe(@Nonnull CheckRecipeResult result,
                                                @Nonnull ProcessingLogic processingLogic) {
        for (var component : postRecipeChecks)
        {
            result = component.postCheckRecipe(result, processingLogic);
        }
        return result;
    }

    protected void setEnergyUsage(ProcessingLogic processingLogic) {
        mEUt = processingLogic.getCalculatedEut();
        if (mEUt > 0) {
            mEUt = (-mEUt);
        }
    }

    public void updateSlots() {
        for (ISlotUpdateAwareComponent component : slotUpdateAwares) {
            component.updateSlots();
        }
    }

    /**
     * Checks recipe and setup machine if it's successful.
     * <p>
     * For generic machine working with recipemap.
     */
    @Nonnull
    public CheckRecipeResult checkProcessing() {
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

    /**
     * Starts checking recipe with some operations needed to actually run the check. Overriding this without due care
     * may result in dupe of items, hence it's marked as final.
     * <p>
     * See {@link #checkProcessing()} for what you want to override.
     *
     * @return If successfully found recipe and/or started processing
     */
    protected final boolean checkRecipe() {
        startRecipeProcessing();
        CheckRecipeResult result = checkProcessing();
        if (!CheckRecipeResultRegistry.isRegistered(result.getID())) {
            throw new RuntimeException(String.format("Result %s is not registered for registry", result.getID()));
        }
        this.checkRecipeResult = result;
        endRecipeProcessing();
        // Don't use `result` here because `endRecipeProcessing()` might mutate `this.checkRecipeResult`
        boolean success = this.checkRecipeResult.wasSuccessful();
        // Arm the fail cooldown (see recipeCheckFailCooldown) that defers THROTTLED rechecks (ME restock, power
        // trickle). IMMEDIATE pushes ignore it, so only machines with throttleable sources are affected, and the
        // default of 0 keeps everything instant.
        if (success) {
            recipeCheckCooldownUntil = 0;
            for (var component : onRecipeStarts) {
                component.onRecipeStart(this);
            }
        } else {
            int cooldown = MachineStats.machines.recipeCheckFailCooldown;
            recipeCheckCooldownUntil = cooldown > 0 ? getTotalRuntime() + cooldown : 0;
        }
        return success;
    }

    protected boolean shouldCheckRecipeThisTick(long aTick) {
        // Recipe checks are purely event-driven: hatches and config changes push via scheduleRecipeCheck(reason)
        // instead of being polled on a periodic timer.
        if (recipeCheckImmediately) {
            // An immediate push (new inputs, drained output, user/structure change) always runs, and covers any
            // pending throttled push too.
            recipeCheckImmediately = false;
            recipeCheckThrottled = false;
            return true;
        }
        if (recipeCheckThrottled && getTotalRuntime() >= recipeCheckCooldownUntil) {
            // A throttleable push (ME restock, power trickle) runs once the post-failure cooldown has expired.
            recipeCheckThrottled = false;
            return true;
        }
        long timeElapsed = getTotalRuntime() - mLastWorkingTick;
        for (var component : recipePollings)
        {
            if (component.shouldPollRecipe(getTotalRuntime(), timeElapsed)) {
                return true;
            }
        }
        return false;
    }

    protected void runMachine(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (!tryFlushPendingOutputs()) return;
        if (mMaxProgresstime > 0) {
            if (mRuntime++ > 1000) mRuntime = 0;
            for (var component : onRecipeTicks) {
                if (!component.onRecipeTick(this)) return;
            }
            markDirty();
            incrementProgressTime();
            if (mProgresstime >= mMaxProgresstime) {
                for (var component : onRecipeFinishes) {
                    component.onRecipeFinish(this);
                }
                if (mOutputItems != null) addPendingOutputs(mOutputItems);
                if (mOutputFluids != null) addPendingOutputs(mOutputFluids);
                mOutputItems = null;
                mOutputFluids = null;
                outputAfterRecipe();
                mOutputItems = null;
                mProgresstime = 0;
                mMaxProgresstime = 0;
                recipesDone += Math.max(processingLogic.getCurrentParallels(), lastParallel);
                mLastWorkingTick = getTotalRuntime();
                if (mPendingItems.isEmpty() && mPendingFluids.isEmpty()
                    && aBaseMetaTileEntity.isAllowedToWork()) {
                    checkRecipe();
                }
            }
        } else {
            // Check if the machine is enabled in the first place!
            if (aBaseMetaTileEntity.isAllowedToWork()) {

                // shouldCheckRecipeThisTick() consumes pending pushes and applies the post-failure cooldown to
                // throttled ones; hasInventoryBeenModified() covers a change to the controller's own inventory slot.
                if (aBaseMetaTileEntity.hasWorkJustBeenEnabled() || shouldCheckRecipeThisTick(aTick)
                    || aBaseMetaTileEntity.hasInventoryBeenModified()) {
                    if (checkRecipe()) {
                        markDirty();
                    }
                }
                if (mMaxProgresstime <= 0)
                {
                    for (var component : onRecipeIdles) {
                        component.onRecipeIdle(this);
                    }
                }
            }
        }
    }

    @Override
    public void postServerTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (!isMachineFormed()) return;
        if (maintenanceLogic != null && maintenanceLogic.getRepairStatus() <= 0) return;
        runMachine(aBaseMetaTileEntity, aTick);
    }

    @Override
    public @NotNull ServerTickPriority getServerTickPriority() {
        return ServerTickPriority.RECIPE;
    }

    private boolean canUseControllerSlotForRecipe = true;

    public boolean canUseControllerSlotForRecipe() {
        return canUseControllerSlotForRecipe;
    }

    public StandardRecipeLogicComponent setCanUseControllerSlotForRecipe(boolean canUseControllerSlotForRecipe) {
        this.canUseControllerSlotForRecipe = canUseControllerSlotForRecipe;
        return this;
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var aNBT = new NBTTagCompound();
        aNBT.setLong("mEUt", mEUt);
        aNBT.setInteger("mRuntime", mRuntime);
        aNBT.setInteger("mProgresstime", mProgresstime);
        aNBT.setInteger("mMaxProgresstime", mMaxProgresstime);
        aNBT.setLong("mLastWorkingTick", mLastWorkingTick);
        aNBT.setLong("recipesDone", recipesDone);
        aNBT.setString("checkRecipeResultID", checkRecipeResult.getID());
        aNBT.setTag("checkRecipeResult", checkRecipeResult.writeToNBT(new NBTTagCompound()));
        return null;
    }

    private void loadNBTDataInternal(@NotNull NBTTagCompound aNBT) {
        mRuntime = aNBT.getInteger("mRuntime");
        mProgresstime = aNBT.getInteger("mProgresstime");
        mMaxProgresstime = aNBT.getInteger("mMaxProgresstime");
        mLastWorkingTick = aNBT.getLong("mLastWorkingTick");
        recipesDone = aNBT.getLong("recipesDone");
        String checkRecipeResultID = aNBT.getString("checkRecipeResultID");
        if (CheckRecipeResultRegistry.isRegistered(checkRecipeResultID)) {
            CheckRecipeResult result = CheckRecipeResultRegistry.getSampleFromRegistry(checkRecipeResultID)
                .newInstance();
            result.readFromNBT(aNBT.getCompoundTag("checkRecipeResult"));
            checkRecipeResult = result;
        }
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        mEUt = nbt.getLong("mEUt");
        loadNBTDataInternal(nbt);
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound aNBT) {
        mEUt = aNBT.getInteger("mEUt");
        loadNBTDataInternal(aNBT);
    }

    private @Nullable IVoidingConfigComponent voidingConfig;
    private @Nullable IMultiblockItemOutputLogicComponent itemOutputLogic;
    private @Nullable IMultiblockFluidOutputLogicComponent fluidOutputLogic;
    private @Nullable IMaintenanceLogicComponent maintenanceLogic;
    private List<IRecipeProcessAwareComponent> recipeProcessAwares;
    private List<ISlotUpdateAwareComponent> slotUpdateAwares;
    private List<IOnRecipeStartComponent> onRecipeStarts;
    private List<IOnRecipeTickComponent> onRecipeTicks;
    private List<IOnRecipeFinishComponent> onRecipeFinishes;
    private List<IOnRecipeIdleComponent> onRecipeIdles;
    private List<IRecipeCheckHandlerComponent> recipeCheckHandlers;
    private List<IProcessingLogicSetupComponent> processingLogicSetups;
    private List<IPostRecipeCheckComponent> postRecipeChecks;
    private List<IRecipePollingComponent> recipePollings;
    @Override
    public void onComponentsReady()
    {
        voidingConfig = tryGetComponent(IVoidingConfigComponent.class);
        itemOutputLogic = tryGetComponent(IMultiblockItemOutputLogicComponent.class);
        fluidOutputLogic = tryGetComponent(IMultiblockFluidOutputLogicComponent.class);
        maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.class);
        recipeProcessAwares = getComponents(IRecipeProcessAwareComponent.class);
        slotUpdateAwares = getComponents(ISlotUpdateAwareComponent.class);
        onRecipeStarts = getComponents(IOnRecipeStartComponent.class);
        onRecipeTicks = getComponents(IOnRecipeTickComponent.class);
        onRecipeFinishes = getComponents(IOnRecipeFinishComponent.class);
        onRecipeIdles = getComponents(IOnRecipeIdleComponent.class);
        recipeCheckHandlers = getComponents(IRecipeCheckHandlerComponent.class);
        processingLogicSetups = getComponents(IProcessingLogicSetupComponent.class);
        postRecipeChecks = getComponents(IPostRecipeCheckComponent.class);
        recipePollings = getComponents(IRecipePollingComponent.class);
    }
}
