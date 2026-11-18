package gregtech.api.ec.multiblock;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IOnMachineRemovalComponent;
import gregtech.api.ec.IOnMachineUnloadComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IHatchContainerComponent
    extends IMultiblockComponent, IPreStructureCheckComponent, IOnMachineUnloadComponent, IOnMachineRemovalComponent {

    ComponentKey.ListKey<IHatchContainerComponent> KEY = ComponentKey.list();

    void detachAllHatches();

    void explodeAllHatches(long power);

    @Override
    default void onPreStructureCheck(@NotNull IGregTechTileEntity te) {
        detachAllHatches();
    }

    @Override
    default void onMachineUnload() {
        detachAllHatches();
    }

    @Override
    default void onMachineRemoval() {
        detachAllHatches();
    }
}
