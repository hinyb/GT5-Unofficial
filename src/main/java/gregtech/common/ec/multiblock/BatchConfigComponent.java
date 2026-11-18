package gregtech.common.ec.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IOnWireCutterClickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IBatchConfigComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.util.GTUtility;
import gregtech.common.config.Gregtech;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class BatchConfigComponent extends BaseMultiblockComponent
    implements IBatchConfigComponent, ISavableComponent, IProcessingLogicSetupComponent, IOnWireCutterClickComponent {

    private boolean batchMode = Gregtech.general.batchModeInitialValue;
    private int maxBatchSize = 128;
    protected static final String BATCH_MODE_NBT_KEY = "batchMode";

    @Override
    public boolean isBatchModeEnabled() {
        return batchMode;
    }

    @Override
    public IBatchConfigComponent setBatchMode(boolean enabled) {
        this.batchMode = enabled;
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            recipeLogic.scheduleRecipeCheck(RecipeCheckReason.IMMEDIATE);
        }
        return this;
    }

    @Override
    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    @Override
    public IBatchConfigComponent setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    @Override
    public @NotNull String getSaveKey() {
        return "batch";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setBoolean(BATCH_MODE_NBT_KEY, batchMode);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        batchMode = nbt.getBoolean(BATCH_MODE_NBT_KEY);
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        batchMode = nbt.getBoolean(BATCH_MODE_NBT_KEY);
    }

    @Override
    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setBatchSize(isBatchModeEnabled() ? getMaxBatchSize() : 1);
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        if (aPlayer.isSneaking()) {
            setBatchMode(!isBatchModeEnabled());
            if (batchMode) {
                GTUtility.sendChatTrans(aPlayer, "GT5U.chat.machine.batch_mode.enable");
            } else {
                GTUtility.sendChatTrans(aPlayer, "GT5U.chat.machine.batch_mode.disable");
            }
            return true;
        }
        return false;
    }
}
