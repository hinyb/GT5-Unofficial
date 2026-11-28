package gregtech.api.ec.signal;

import org.jetbrains.annotations.NotNull;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@FunctionalInterface
public interface IOnActiveStateChange {

    void onActiveStateChanged(@NotNull IGregTechTileEntity igte, boolean active);
}
