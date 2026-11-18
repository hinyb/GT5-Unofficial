package gregtech.api.ec.gui;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ListWidget;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IGuiSyncableComponent;

public interface ITerminalTextWidgetComponent extends IGuiSyncableComponent {

    ComponentKey.ListKey<ITerminalTextWidgetComponent> KEY = ComponentKey.list();

    void addTerminalTextWidget(@NotNull ListWidget<IWidget, ?> widgetList, PanelSyncManager syncManager,
        ModularPanel parent);
}
