package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_SCREWDRIVER_CLICK;
import static gregtech.api.ec.IECMachine.WAILA_DATA_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_SYNCABLE;
import static gregtech.api.ec.component.IProcessingLogicRecipeComponent.PROCESSING_LOGIC_SETUP;
import static gregtech.api.recipe.check.SingleRecipeCheck.getDisplayString;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.ISavableComponent;
import gregtech.api.ec.component.multiblock.IRecipeLockingComponent;
import gregtech.api.ec.gui.MultiblockGuiBuilder;
import gregtech.api.ec.signal.IGuiProvider;
import gregtech.api.ec.signal.IOnToolClick;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.recipe.check.SingleRecipeCheck;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockECBaseGui;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class RecipeLockingComponent extends BaseMultiblockComponent
    implements IRecipeLockingComponent, ISavableComponent {

    private boolean mLockedToSingleRecipe = false;
    private @Nullable SingleRecipeCheck mSingleRecipeCheck = null;
    private boolean allowScrewdriverToggle = true;

    @Override
    public boolean isRecipeLockingEnabled() {
        return mLockedToSingleRecipe;
    }

    @Override
    public void setRecipeLocking(boolean enabled) {
        mLockedToSingleRecipe = enabled;
        if (!enabled) {
            setSingleRecipeCheck(null);
        }
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            recipeLogic.scheduleRecipeCheck(RecipeCheckReason.IMMEDIATE);
        }
    }

    @Override
    public SingleRecipeCheck getSingleRecipeCheck() {
        return mSingleRecipeCheck;
    }

    @Override
    public void setSingleRecipeCheck(@Nullable SingleRecipeCheck recipeCheck) {
        mSingleRecipeCheck = recipeCheck;
    }

    public RecipeLockingComponent setAllowScrewdriverToggle(boolean allow) {
        allowScrewdriverToggle = allow;
        return this;
    }

    @Override
    public @NotNull String getSaveKey() {
        return "lock";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("mLockedToSingleRecipe", mLockedToSingleRecipe);
        if (mLockedToSingleRecipe && mSingleRecipeCheck != null)
            nbt.setTag("mSingleRecipeCheck", mSingleRecipeCheck.writeToNBT());
        return nbt;
    }

    protected SingleRecipeCheck loadSingleRecipeChecker(NBTTagCompound aNBT) {
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        return recipeLogic != null ? SingleRecipeCheck.tryLoad(recipeLogic.getRecipeMap(), aNBT) : null;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        mLockedToSingleRecipe = nbt.getBoolean("mLockedToSingleRecipe");
        if (mLockedToSingleRecipe && nbt.hasKey("mSingleRecipeCheck", Constants.NBT.TAG_COMPOUND)) {
            SingleRecipeCheck c = loadSingleRecipeChecker(nbt.getCompoundTag("mSingleRecipeCheck"));
            if (c != null) mSingleRecipeCheck = c;
            // the old recipe is gone. we disable the machine to prevent making garbage in case of shared inputs
            // maybe use a better way to inform player in the future.
            else getMachine().disableWorking();
        }
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        loadLegacyNBTData(nbt);
    }

    @Override
    public int getSavePriority() {
        return SavePriority.RECIPE_LOCKING.priority;
    }

    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setRecipeLocking(getMachine(), isRecipeLockingEnabled());
    }

    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        tag.setBoolean("isLockedToRecipe", isRecipeLockingEnabled());
        SingleRecipeCheck lockedRecipe = getSingleRecipeCheck();
        tag.setString(
            "lockedRecipeName",
            lockedRecipe != null ? getDisplayString(lockedRecipe.getRecipe(), false, true, false, true) : "");
    }

    public boolean onScrewdriverRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        if (!allowScrewdriverToggle) return false;
        var state = isRecipeLockingEnabled();
        setRecipeLocking(!state);
        if (!state) {
            GTUtility.sendChatTrans(aPlayer, "gt.interact.desc.mb.locking_on");
        } else {
            GTUtility.sendChatTrans(aPlayer, "gt.interact.desc.mb.locking_off");
            setSingleRecipeCheck(null);
        }
        return true;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_SCREWDRIVER_CLICK, IOnToolClick.ScrewdriverPriority.RECIPE_LOCKING, this::onScrewdriverRightClick);
        connect(PROCESSING_LOGIC_SETUP, this::setupProcessingLogic);
        connect(WAILA_DATA_PROVIDER, this::appendWailaNBTData);
        connect(GUI_PROVIDER, this::configureGui);
        connect(GUI_SYNCABLE, this::registerGuiSyncValues);
    }

    public void registerGuiSyncValues(@NotNull PanelSyncManager syncManager) {
        BooleanSyncValue recipeLockSyncer = new BooleanSyncValue(this::isRecipeLockingEnabled, this::setRecipeLocking)
            .allowC2S();
        syncManager.syncValue("recipeLock", recipeLockSyncer);
    }

    public void configureGui(@NotNull MultiblockGuiBuilder builder) {
        builder.leftGap()
            .replace(
                IGuiProvider.LeftGapSection.RECIPE_LOCK,
                (panel, syncManager) -> MTEMultiBlockECBaseGui.createActiveFeatureToggleButton(
                    syncManager.findSyncHandler("recipeLock", BooleanSyncValue.class),
                    GTGuiTextures.OVERLAY_BUTTON_RECIPE_UNLOCKED,
                    GTGuiTextures.OVERLAY_BUTTON_RECIPE_LOCKED,
                    "GT5U.gui.button.lock_recipe",
                    this::isRecipeLockingEnabled));
    }
}
