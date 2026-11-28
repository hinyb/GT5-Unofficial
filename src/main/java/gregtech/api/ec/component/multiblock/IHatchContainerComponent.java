package gregtech.api.ec.component.multiblock;

import static gregtech.api.ec.IECMachine.ON_MACHINE_REMOVAL;
import static gregtech.api.ec.IECMachine.ON_MACHINE_UNLOAD;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.PRE_STRUCTURE_CHECK;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.SignalKey;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

public interface IHatchContainerComponent extends IMultiblockComponent {

    ComponentKey.ListKey<IHatchContainerComponent> KEY = ComponentKey.list();

    @FunctionalInterface
    interface IOnHatchAttached {

        void onHatchAttached(@NotNull IMetaTileEntity metaTile, int baseCasingIndex);
    }

    SignalKey<IOnHatchAttached> ON_HATCH_ATTACHED = SignalKey.create();

    void detachAllHatches();

    void explodeAllHatches(long power);

    default void connectHatchLifecycle() {
        connect(ON_MACHINE_REMOVAL, this::detachAllHatches);
        connect(ON_MACHINE_UNLOAD, this::detachAllHatches);
        connect(PRE_STRUCTURE_CHECK, (_) -> detachAllHatches());
    }
}
