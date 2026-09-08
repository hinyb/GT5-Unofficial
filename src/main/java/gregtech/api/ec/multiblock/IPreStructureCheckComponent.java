package gregtech.api.ec.multiblock;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IPreStructureCheckComponent extends IMultiblockComponent {
    void onPreStructureCheck(IGregTechTileEntity te);
}
