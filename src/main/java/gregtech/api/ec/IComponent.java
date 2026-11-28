package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;

public interface IComponent {

    ComponentKey.ListKey<IComponent> KEY = ComponentKey.list();

    default String getComponentName() {
        return this.getClass()
            .getSimpleName();
    }

    void onAddToMachine(@NotNull IECMachine machine);

    void onRemoveFromMachine(@NotNull IECMachine machine);

    @NotNull
    IECMachine getMachine();

    default void onComponentsReady() {}

    default <T> @NotNull Signal<T> signal(@NotNull SignalKey<T> key) {
        return getMachine().signal(key);
    }

    default <T> @NotNull T[] listeners(@NotNull SignalKey<T> key) {
        return getMachine().listeners(key);
    }

    default <T> @NotNull Connection connect(@NotNull SignalKey<T> key, @NotNull T listener) {
        return connect(key, 0, listener);
    }

    default <T> @NotNull Connection connect(@NotNull SignalKey<T> key, int priority, @NotNull T listener) {
        return getMachine().signal(key)
            .connect(this, priority, listener);
    }

    default <T> @NotNull Connection watchActive(@NotNull SignalKey<T> key, @NotNull Signal.ActiveListener<T> listener) {
        return watchActive(key, 0, listener);
    }

    default <T> @NotNull Connection watchActive(@NotNull SignalKey<T> key, int priority,
        @NotNull Signal.ActiveListener<T> listener) {
        return getMachine().signal(key)
            .watchActive(this, priority, listener);
    }
}
