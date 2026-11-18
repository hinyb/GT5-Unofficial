package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IPreRunMachineComponent extends IComponent {

    ComponentKey.ListKey<IPreRunMachineComponent> KEY = ComponentKey.list();

    boolean onPreRunMachine(@NotNull IRecipeLogicComponent recipeLogic, IGregTechTileEntity igte, long tick);
}
