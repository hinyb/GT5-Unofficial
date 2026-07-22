package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IECMachine {
    default <T extends IComponent> @Nullable T tryGetComponent(@NotNull Class<T> type) {
        List<T> list = getComponents(type);
        return list.isEmpty() ? null : list.getFirst();
    }

    default <T extends IComponent> @NotNull T getComponent(@NotNull Class<T> type) {
        T component = tryGetComponent(type);
        if (component == null)
        {
            throw new IllegalStateException("Missing component: " + type.getSimpleName());
        }
        return component;
    }
    default <T extends IComponent> boolean hasComponent(@NotNull Class<T> type) {
        return !getComponents(type).isEmpty();
    }
    <T extends IComponent> @NotNull List<T> getComponents(@NotNull Class<T> type);
    <T extends IComponent> void addComponent(@NotNull T component);
}
