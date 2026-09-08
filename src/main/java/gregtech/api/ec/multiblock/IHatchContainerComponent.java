package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;
import gregtech.api.ec.IOnMachineRemovalComponent;
import gregtech.api.ec.IOnMachineUnloadComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import org.jetbrains.annotations.NotNull;

public interface IHatchContainerComponent extends IMultiblockComponent, IPreStructureCheckComponent, IOnMachineUnloadComponent, IOnMachineRemovalComponent {
    boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex);
    void detachAllHatches();
    void explodeAllHatches(long power);
    @Override
    default void onPreStructureCheck(IGregTechTileEntity te) {
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
