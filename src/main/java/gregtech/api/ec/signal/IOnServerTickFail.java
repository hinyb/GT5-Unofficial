package gregtech.api.ec.signal;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@FunctionalInterface
public interface IOnServerTickFail {

    void onServerTickFail(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
}
