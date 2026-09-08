package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IPostStructureCheckComponent extends IMultiblockComponent {
    boolean onPostStructureCheck(@NotNull IGregTechTileEntity te, @NotNull List<StructureError> errors);
}
