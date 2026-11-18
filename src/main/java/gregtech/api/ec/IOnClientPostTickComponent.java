package gregtech.api.ec;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IOnClientPostTickComponent extends IComponent {

    ComponentKey.ListKey<IOnClientPostTickComponent> KEY = ComponentKey.list();

    void onClientPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
}
