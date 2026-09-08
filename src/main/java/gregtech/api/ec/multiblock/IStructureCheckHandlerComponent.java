package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IStructureCheckHandlerComponent extends IMultiblockComponent {
    boolean tryCheckMachine(@NotNull IGregTechTileEntity aBaseMetaTileEntity, @NotNull List<StructureError> errors);
}
