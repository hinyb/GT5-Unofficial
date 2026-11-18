package gregtech.api.ec.multiblock;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;

public interface IRecipeEfficiencyLogicComponent extends IMultiblockComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IRecipeEfficiencyLogicComponent> KEY = ComponentKey.unique();

    int getEfficiency();

    int getMaxEfficiency();

    IRecipeEfficiencyLogicComponent setEfficiency(int efficiency);

    IRecipeEfficiencyLogicComponent setEfficiencyDecrease(int efficiencyDecrease);

    IRecipeEfficiencyLogicComponent setEfficiencyIncrease(int efficiencyIncrease);
}
