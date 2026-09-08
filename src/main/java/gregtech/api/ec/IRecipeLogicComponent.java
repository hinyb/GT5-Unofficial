package gregtech.api.ec;

import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.RecipeCheckReason;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLogicComponent extends IUniqueComponent {
    @NotNull RecipeMap<?> getRecipeMap();
    long getEUt();
    int getProgressTime();
    int getMaxProgressTime();
    int getRuntime();
    CheckRecipeResult getCheckRecipeResult();
    void setCheckRecipeResult(CheckRecipeResult result);
    void scheduleRecipeCheck(RecipeCheckReason reason);
    @Nullable ItemStack[] getOutputItems();
    @Nullable FluidStack[] getOutputFluids();
}
