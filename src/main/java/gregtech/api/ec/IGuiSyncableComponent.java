package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public interface IGuiSyncableComponent extends IComponent {

    ComponentKey.ListKey<IGuiSyncableComponent> KEY = ComponentKey.list();

    void registerGuiSyncValues(@NotNull PanelSyncManager syncManager);
}
