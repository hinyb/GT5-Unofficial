package gregtech.api.ec;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public interface IOnServerTickFailComponent extends IComponent {

    ComponentKey.ListKey<IOnServerTickFailComponent> KEY = ComponentKey.list();

    void onServerTickFail(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
}
