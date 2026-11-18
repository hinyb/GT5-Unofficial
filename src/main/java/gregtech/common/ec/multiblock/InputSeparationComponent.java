package gregtech.common.ec.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IOnScrewdriverClickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IInputSeparationComponent;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class InputSeparationComponent extends BaseMultiblockComponent
    implements IInputSeparationComponent, ISavableComponent, IOnScrewdriverClickComponent {

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

    @Override
    public boolean onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        if (!allowScrewdriverToggle) return false;
        setInputSeparation(!isInputSeparationEnabled());
        GTUtility.sendChatTrans(
            aPlayer,
            inputSeparation ? "GT5U.machines.separatebus.true" : "GT5U.machines.separatebus.false");
        return true;
    }
}
