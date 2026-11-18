package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IOnFirstTickComponent extends IComponent {

    ComponentKey.ListKey<IOnFirstTickComponent> KEY = ComponentKey.list();

    void onFirstTick(@NotNull IGregTechTileEntity igte);
}
