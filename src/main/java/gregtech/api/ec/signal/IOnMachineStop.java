package gregtech.api.ec.signal;

import org.jetbrains.annotations.NotNull;

import gregtech.api.util.shutdown.ShutDownReason;

@FunctionalInterface
public interface IOnMachineStop {

    void onMachineStop(@NotNull ShutDownReason reason);
}
