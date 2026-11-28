package gregtech.api.ec.signal;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@FunctionalInterface
public interface IOnClientPostTick {

    void onClientPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
}
