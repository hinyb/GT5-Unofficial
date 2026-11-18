package gregtech.api.ec.multiblock;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;

public interface IStructureCheckHandlerComponent extends IMultiblockComponent {

    ComponentKey.ListKey<IStructureCheckHandlerComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IStructureCheckHandlerComponent::getStructureCheckPriority));

    enum StructureCheckPriority {

        MACHINE_CHECK(1000),
        POLLUTION(2000),
        COIL(3000);

        public final int priority;

        StructureCheckPriority(int priority) {
            this.priority = priority;
        }
    }

    void checkMachine(@NotNull IGregTechTileEntity aBaseMetaTileEntity, @NotNull List<StructureError> errors);

    int getStructureCheckPriority();
}
