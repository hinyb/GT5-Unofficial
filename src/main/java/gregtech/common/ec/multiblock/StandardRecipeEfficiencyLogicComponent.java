package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeFinishComponent;
import gregtech.api.ec.IOnRecipeIdleComponent;
import gregtech.api.ec.IOnRecipeStartComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;

public class StandardRecipeEfficiencyLogicComponent extends BaseMultiblockComponent implements IRecipeEfficiencyLogicComponent, IOnRecipeStartComponent, IOnRecipeFinishComponent, IOnRecipeIdleComponent, ISavableComponent {
    @Override
    public void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        mEfficiency = Math.clamp(mEfficiency + mEfficiencyIncrease, 0, getMaxEfficiency());
        mEfficiencyIncrease = 0;
    }

    @Override
    public @NotNull RecipeFinishPriority getRecipeFinishPriority() {
        return RecipeFinishPriority.EFFICIENCY;
    }

    @Override
    public void onRecipeIdle(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        mEfficiency = Math.max(0, mEfficiency - mEfficiencyDecrease);
    }

    @Override
    public @NotNull RecipeIdlePriority getRecipeIdlePriority() {
        return RecipeIdlePriority.EFFICIENCY;
    }

    @Override
    public void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        mEfficiency = 10000;
        var maintainComp = tryGetComponent(IMaintenanceLogicComponent.class);
        if (maintainComp != null) {
            mEfficiency -= maintainComp.getMaintenanceEfficiencyPenalty();
        }
        mEfficiencyIncrease = 10000;
    }

    @Override
    public @NotNull RecipeStartPriority getRecipeStartPriority() {
        return RecipeStartPriority.EFFICIENCY;
    }

    @FunctionalInterface
    public interface IMaxEfficiencyCalculator {
        @Range(from = 0, to = Integer.MAX_VALUE)
        int getMaxEfficiency(@Nullable ItemStack aStack);
    }
    private IMaxEfficiencyCalculator calculator = stack -> 10000;
    private int mEfficiency = 0;
    private int mEfficiencyIncrease = 10000;
    private int mEfficiencyDecrease = 0;
    @Override
    public int getEfficiency() {
        return mEfficiency;
    }

    @Override
    public StandardRecipeEfficiencyLogicComponent setEfficiency(int efficiency) {
        mEfficiency = efficiency;
        return this;
    }

    @Override
    public int getMaxEfficiency() {
        int max = calculator.getMaxEfficiency(getMachine().getControllerSlot());
        if (maintenanceLogic != null)
        {
            max -= maintenanceLogic.getMaintenanceEfficiencyPenalty();
        }
        return max;
    }

    public StandardRecipeEfficiencyLogicComponent setMaxEfficiencyCalculator(IMaxEfficiencyCalculator calculator)
    {
        this.calculator = calculator;
        return this;
    }

    @Override
    public StandardRecipeEfficiencyLogicComponent setEfficiencyDecrease(int mEfficiencyDecrease)
    {
        this.mEfficiencyDecrease = mEfficiencyDecrease;
        return this;
    }

    @Override
    public StandardRecipeEfficiencyLogicComponent setEfficiencyIncrease(int efficiencyIncrease)
    {
        this.mEfficiencyIncrease = efficiencyIncrease;
        return this;
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var aNBT = new NBTTagCompound();
        aNBT.setInteger("mEfficiencyIncrease", mEfficiencyIncrease);
        aNBT.setInteger("mEfficiencyDecrease", mEfficiencyDecrease);
        aNBT.setInteger("mEfficiency", mEfficiency);
        return aNBT;
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound aNBT) {
        mEfficiencyIncrease = aNBT.getInteger("mEfficiencyIncrease");
        mEfficiencyDecrease = aNBT.getInteger("mEfficiencyDecrease");
        mEfficiency = aNBT.getInteger("mEfficiency");
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound aNBT) {
        loadComponentData(aNBT);
    }
    private @Nullable IMaintenanceLogicComponent maintenanceLogic;
    @Override
    public void onComponentsReady()
    {
        maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.class);
    }
}
