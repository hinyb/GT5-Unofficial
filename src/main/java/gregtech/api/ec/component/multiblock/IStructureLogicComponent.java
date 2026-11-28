package gregtech.api.ec.component.multiblock;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.SignalKey;
import gregtech.api.ec.component.IUniqueComponent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;

public interface IStructureLogicComponent extends IMultiblockComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IStructureLogicComponent> KEY = ComponentKey.unique();

    boolean isFormed();

    void markStructureDirty();

    void scheduleCheckNextTick();

    boolean checkStructure(IGregTechTileEntity te);

    List<StructureError> getStructureErrors();

    int getStructureUpdateTime();

    void setStructureUpdateTime(int time);

    int getStartUpCheckTimer();

    @FunctionalInterface
    interface IOnStructureStateChange {

        void onStructureStateChanged(@NotNull IGregTechTileEntity igte);
    }

    SignalKey<IOnStructureStateChange> ON_STRUCTURE_FORMED = SignalKey.create();
    SignalKey<IOnStructureStateChange> ON_STRUCTURE_BROKEN = SignalKey.create();

    @FunctionalInterface
    interface IPreStructureCheck {

        void onPreStructureCheck(@NotNull IGregTechTileEntity te);
    }

    SignalKey<IPreStructureCheck> PRE_STRUCTURE_CHECK = SignalKey.create();

    @FunctionalInterface
    interface IPostStructureCheck {

        void onPostStructureCheck(@NotNull IGregTechTileEntity te, @NotNull List<StructureError> errors);

        int STRUCTURE_BOUNDS = -2000;
        int COIL = -1000;
    }

    SignalKey<IPostStructureCheck> POST_STRUCTURE_CHECK = SignalKey.create();

    @FunctionalInterface
    interface IStructureCheckHandler {

        void checkMachine(@NotNull IGregTechTileEntity aBaseMetaTileEntity, @NotNull List<StructureError> errors);

        int MACHINE_CHECK = -3000;
        int POLLUTION = -2000;
        int COIL = -1000;
    }

    SignalKey<IStructureCheckHandler> ON_STRUCTURE_SCAN = SignalKey.create();
    SignalKey<IStructureCheckHandler> ON_STRUCTURE_VERIFY = SignalKey.create();
}
