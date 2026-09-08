package gregtech.common.ec.multiblock;

import gregtech.api.ec.IEnergyInputLogicComponent;
import gregtech.api.ec.IEnergyOutputLogicComponent;
import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToLongFunction;

// may need a refactor
public class StandardEnergyConsumeComponent extends BaseMultiblockComponent implements IOnRecipeTickComponent {
    private final ToLongFunction<IRecipeLogicComponent> getActualEnergyUsage;

    public StandardEnergyConsumeComponent(ToLongFunction<IRecipeLogicComponent> getActualEnergyUsage) {
        this.getActualEnergyUsage = getActualEnergyUsage;
    }

    public StandardEnergyConsumeComponent() {
        this.getActualEnergyUsage = this::getActualEnergyUsage;
    }

    private int getEfficiency()
    {
        return recipeEfficiencyLogic != null ?recipeEfficiencyLogic.getEfficiency() : 10000;
    }
    private long getActualEnergyUsage(IRecipeLogicComponent recipeLogicComponent) {
        return (-recipeLogicComponent.getEUt() * 10_000) / Math.max(1000, getEfficiency());
    }

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        long mEUt = recipeLogicComponent.getEUt();
        if (mEUt > 0) {
            if (energyOutputLogic == null) {
                throw new IllegalStateException("Recipe produces energy but no energy output component is attached.");
            }
            energyOutputLogic.addEnergyOutput((mEUt * getEfficiency()) / 10000);
            return true;
        }
        if (mEUt < 0) {
            if (energyInputLogic == null) {
                throw new IllegalStateException("Recipe consumes energy but no energy input component is attached.");
            }
            if (!energyInputLogic.drainEnergyInput(getActualEnergyUsage.applyAsLong(recipeLogicComponent))) {
                stopMachine(ShutDownReasonRegistry.POWER_LOSS);
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeTickPriority getRecipeTickPriority() {
        return RecipeTickPriority.CONSUME_ENERGY;
    }

    private @Nullable IEnergyInputLogicComponent energyInputLogic;
    private @Nullable IEnergyOutputLogicComponent energyOutputLogic;
    private @Nullable IRecipeEfficiencyLogicComponent recipeEfficiencyLogic;
    @Override
    public void onComponentsReady() {
        energyInputLogic = tryGetComponent(IEnergyInputLogicComponent.class);
        energyOutputLogic = tryGetComponent(IEnergyOutputLogicComponent.class);
        recipeEfficiencyLogic = tryGetComponent(IRecipeEfficiencyLogicComponent.class);
    }
}
