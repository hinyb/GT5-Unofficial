package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;

import java.util.List;

public interface IStructureLogicComponent extends IMultiblockComponent {
    boolean isFormed();
    void scheduleStructureCheck();
    boolean checkStructure(IGregTechTileEntity te);
    List<StructureError> getStructureErrors();
}
