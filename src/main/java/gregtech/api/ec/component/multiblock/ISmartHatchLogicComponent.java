package gregtech.api.ec.component.multiblock;

import static gregtech.api.ec.IECMachine.ON_MACHINE_REMOVAL;
import static gregtech.api.ec.IECMachine.ON_MACHINE_UNLOAD;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.PRE_STRUCTURE_CHECK;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.component.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

public interface ISmartHatchLogicComponent extends IMultiblockComponent, IUniqueComponent {

    ComponentKey.UniqueKey<ISmartHatchLogicComponent> KEY = ComponentKey.unique();

    boolean tryAttachSmartHatch(@NotNull IMetaTileEntity part, int baseCasingIndex);

    boolean needsPeriodicChecks();

    void detachAllHatches();

    default void connectHatchLifecycle() {
        connect(ON_MACHINE_REMOVAL, this::detachAllHatches);
        connect(ON_MACHINE_UNLOAD, this::detachAllHatches);
        connect(PRE_STRUCTURE_CHECK, (_) -> detachAllHatches());
    }
}
