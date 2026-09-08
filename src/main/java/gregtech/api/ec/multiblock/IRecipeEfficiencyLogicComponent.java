package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.common.ec.multiblock.StandardRecipeEfficiencyLogicComponent;

public interface IRecipeEfficiencyLogicComponent extends IMultiblockComponent, IUniqueComponent {
    int getEfficiency();
    int getMaxEfficiency();
    IRecipeEfficiencyLogicComponent setEfficiency(int efficiency);
    IRecipeEfficiencyLogicComponent setEfficiencyDecrease(int efficiencyDecrease);
    IRecipeEfficiencyLogicComponent setEfficiencyIncrease(int efficiencyIncrease);
}
