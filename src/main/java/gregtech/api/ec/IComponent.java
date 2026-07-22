package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IComponent {
    default String getComponentName() {
        return this.getClass().getSimpleName();
    }
    void onAddToMachine(@NotNull IECMachine machine);
    @Nullable IECMachine getMachine();
}
