package gregtech.api.ec;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface ITickableComponent extends IComponent {
    default void postServerTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {};
    default void postClientTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {};
}
