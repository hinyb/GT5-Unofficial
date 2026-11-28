package gregtech.api.ec.signal;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

@FunctionalInterface
public interface IGuiSyncable {

    void registerGuiSyncValues(@NotNull PanelSyncManager syncManager);
}
