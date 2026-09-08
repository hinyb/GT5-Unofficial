package gregtech.common.ec.multiblock;

import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.ec.multiblock.IRecipeLockingComponent;
import gregtech.api.interfaces.tileentity.IRecipeLockable;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.SingleRecipeCheck;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeLockingComponent extends BaseMultiblockComponent implements IRecipeLockingComponent, ISavableComponent, IProcessingLogicSetupComponent {
    private boolean mLockedToSingleRecipe = false;
    private SingleRecipeCheck mSingleRecipeCheck = null;

    @Override
    public boolean isRecipeLockingEnabled() {
        return mLockedToSingleRecipe;
    }

    @Override
    public void setRecipeLocking(boolean enabled) {
        mLockedToSingleRecipe = enabled;
    }

    @Override
    public SingleRecipeCheck getSingleRecipeCheck() {
        return mSingleRecipeCheck;
    }

    @Override
    public void setSingleRecipeCheck(SingleRecipeCheck recipeCheck) {
        mSingleRecipeCheck = recipeCheck;
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("mLockedToSingleRecipe", mLockedToSingleRecipe);
        if (mLockedToSingleRecipe && mSingleRecipeCheck != null)
            nbt.setTag("mSingleRecipeCheck", mSingleRecipeCheck.writeToNBT());
        return null;
    }

    protected SingleRecipeCheck loadSingleRecipeChecker(NBTTagCompound aNBT) {
        return SingleRecipeCheck.tryLoad(recipeLogic.getRecipeMap(), aNBT);
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
    public void setupProcessingLogic(ProcessingLogic logic) {
        // todo refactor
        logic.setRecipeLocking((IRecipeLockable) getMachine(), isRecipeLockingEnabled());
    }

    private IRecipeLogicComponent recipeLogic;
    @Override
    public void onComponentsReady(){
        recipeLogic = getComponent(IRecipeLogicComponent.class);
    }
}
