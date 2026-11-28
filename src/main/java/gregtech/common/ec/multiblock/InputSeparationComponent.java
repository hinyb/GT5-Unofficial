package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_SCREWDRIVER_CLICK;
import static gregtech.api.ec.IECMultiblock.GUI_PROVIDER;
import static gregtech.api.ec.IECMultiblock.GUI_SYNCABLE;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.ISavableComponent;
import gregtech.api.ec.component.multiblock.IInputSeparationComponent;
import gregtech.api.ec.gui.MultiblockGuiBuilder;
import gregtech.api.ec.signal.IGuiProvider;
import gregtech.api.ec.signal.IOnToolClick;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockECBaseGui;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class InputSeparationComponent extends BaseMultiblockComponent
    implements IInputSeparationComponent, ISavableComponent {

    protected static final String INPUT_SEPARATION_NBT_KEY = "inputSeparation";
    private boolean inputSeparation = true;
    private boolean allowScrewdriverToggle = false;

    @Override
    public boolean isInputSeparationEnabled() {
        return inputSeparation;
    }

    @Override
    public InputSeparationComponent setInputSeparation(boolean enabled) {
        inputSeparation = enabled;
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            recipeLogic.scheduleRecipeCheck(RecipeCheckReason.IMMEDIATE);
        }
        return this;
    }

    public InputSeparationComponent setAllowScrewdriverToggle(boolean allow) {
        allowScrewdriverToggle = allow;
        return this;
    }

    @Override
    public @NotNull String getSaveKey() {
        return "inSep";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean(INPUT_SEPARATION_NBT_KEY, inputSeparation);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        if (nbt.hasKey(INPUT_SEPARATION_NBT_KEY)) inputSeparation = nbt.getBoolean(INPUT_SEPARATION_NBT_KEY);
        else if (nbt.hasKey("isBussesSeparate")) inputSeparation = nbt.getBoolean("isBussesSeparate");
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        inputSeparation = nbt.getBoolean(INPUT_SEPARATION_NBT_KEY);
    }

    public boolean onScrewdriverRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        if (!allowScrewdriverToggle) return false;
        setInputSeparation(!isInputSeparationEnabled());
        GTUtility.sendChatTrans(
            aPlayer,
            inputSeparation ? "GT5U.machines.separatebus.true" : "GT5U.machines.separatebus.false");
        return true;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_SCREWDRIVER_CLICK, IOnToolClick.ScrewdriverPriority.INPUT_SEPARATION, this::onScrewdriverRightClick);
        connect(GUI_PROVIDER, this::configureGui);
        connect(GUI_SYNCABLE, this::registerGuiSyncValues);
    }

    public void registerGuiSyncValues(@NotNull PanelSyncManager syncManager) {
        BooleanSyncValue inputSeparationSyncer = new BooleanSyncValue(
            this::isInputSeparationEnabled,
            this::setInputSeparation).allowC2S();
        syncManager.syncValue("inputSeparation", inputSeparationSyncer);
    }

    public void configureGui(@NotNull MultiblockGuiBuilder builder) {
        builder.leftGap()
            .replace(
                IGuiProvider.LeftGapSection.INPUT_SEPARATION,
                (panel, syncManager) -> MTEMultiBlockECBaseGui.createActiveFeatureToggleButton(
                    syncManager.findSyncHandler("inputSeparation", BooleanSyncValue.class),
                    GTGuiTextures.OVERLAY_BUTTON_INPUT_SEPARATION_OFF,
                    GTGuiTextures.OVERLAY_BUTTON_INPUT_SEPARATION_ON,
                    "GT5U.gui.button.input_separation",
                    this::isInputSeparationEnabled));
    }
}
