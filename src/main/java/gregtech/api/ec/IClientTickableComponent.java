package gregtech.api.ec;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IClientTickableComponent extends IComponent {
    void postClientTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
}
