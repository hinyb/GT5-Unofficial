package gregtech.api.ec.signal;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@FunctionalInterface
public interface IOnServerPostTick {

    void onServerPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick);

    // multiblock
    int STRUCTURE_CHECK = -3000;
    int MAINTAIN = -2000;
    int RECIPE = -1000;
}
