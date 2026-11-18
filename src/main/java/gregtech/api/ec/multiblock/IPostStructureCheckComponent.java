package gregtech.api.ec.multiblock;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;

public interface IPostStructureCheckComponent extends IMultiblockComponent {

    ComponentKey.ListKey<IPostStructureCheckComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IPostStructureCheckComponent::getPostStructureCheckPriority));

    enum PostStructureCheckPriority {

        RECIPE(1000),
        POLLUTION(2000),
        COIL(3000);

        public final int priority;

        PostStructureCheckPriority(int priority) {
            this.priority = priority;
        }
    }

    void onPostStructureCheck(@NotNull IGregTechTileEntity te, @NotNull List<StructureError> errors);

    int getPostStructureCheckPriority();
}
