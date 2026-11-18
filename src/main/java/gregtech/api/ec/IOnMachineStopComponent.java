package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

import gregtech.api.util.shutdown.ShutDownReason;

public interface IOnMachineStopComponent extends IComponent {

    ComponentKey.ListKey<IOnMachineStopComponent> KEY = ComponentKey.list();

    void onMachineStop(@NotNull ShutDownReason reason);
}
