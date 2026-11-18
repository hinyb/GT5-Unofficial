package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IPostRunMachineComponent extends IComponent {

    ComponentKey.ListKey<IPostRunMachineComponent> KEY = ComponentKey.list();

    void onPostRunMachine(@NotNull IRecipeLogicComponent recipeLogic, IGregTechTileEntity igte, long tick);
}
