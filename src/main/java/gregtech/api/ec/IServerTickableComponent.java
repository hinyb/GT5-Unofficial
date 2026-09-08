package gregtech.api.ec;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import org.jetbrains.annotations.NotNull;

public interface IServerTickableComponent extends IComponent {
    enum ServerTickPriority {
        STRUCTURE_CHECK,
        MAINTAIN,
        RECIPE,
        COIL
    }
    void postServerTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick);
    @NotNull ServerTickPriority getServerTickPriority();
}
