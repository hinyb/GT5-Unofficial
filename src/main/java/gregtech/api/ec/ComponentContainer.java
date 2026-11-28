package gregtech.api.ec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.internal.BitIndexedArray;

public final class ComponentContainer {

    private final @NotNull BitIndexedArray<SignalKey<?>> signals = new BitIndexedArray<>();
    private final @NotNull BitIndexedArray<ComponentKey<?>> components = new BitIndexedArray<>();
    private final @NotNull IECMachine owner;
    private final @NotNull Runnable onHostReady;

    public ComponentContainer(@NotNull IECMachine owner, @NotNull Runnable onHostReady) {
        this.owner = owner;
        this.onHostReady = onHostReady;
    }

    private enum LifecycleState {
        UNINITIALIZED,
        INITIALIZING,
        READY
    }

    private LifecycleState state = LifecycleState.UNINITIALIZED;

    public boolean isInitialized() {
        return state != LifecycleState.UNINITIALIZED;
    }

    public boolean isInitializing() {
        return state == LifecycleState.INITIALIZING;
    }

    public boolean isReady() {
        return state == LifecycleState.READY;
    }

    public void initialize(@NotNull Runnable register) {
        if (state != LifecycleState.UNINITIALIZED) return;
        state = LifecycleState.INITIALIZING;
        register.run();
        state = LifecycleState.READY;
        notifyReady();
    }

    private boolean notifyDirty = false;
    private boolean readyDirty = false;

    private void notifyReady() {
        if (notifyDirty) {
            readyDirty = true;
            return;
        }
        notifyDirty = true;
        do {
            readyDirty = false;
            onHostReady.run();
            for (var component : getComponents(IComponent.KEY)) {
                component.onComponentsReady();
            }
        } while (readyDirty);
        notifyDirty = false;
    }

    @SuppressWarnings("unchecked")
    public <T extends IComponent> @Nullable T tryGetComponent(@NotNull ComponentKey.UniqueKey<T> key) {
        return (T) components.tryGet(key);
    }

    public <T extends IComponent> boolean hasComponent(@NotNull ComponentKey<T> key) {
        return components.contains(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends IComponent> @NotNull T[] getComponents(@NotNull ComponentKey.ListKey<T> key) {
        var value = components.tryGet(key);
        return value != null ? (T[]) value : key.emptyArray;
    }

    public <T extends IComponent> void addComponent(@NotNull T component) {
        for (var c : getComponents(IComponent.KEY)) {
            if (c == component) {
                throw new IllegalStateException("Can't add duplicate component instance: "+ component.getClass().getSimpleName());
            }
        }
        var keys = ComponentKey.getKeysForImpl(component.getClass());
        for (var key : keys) {
            if (key instanceof ComponentKey.UniqueKey<?> uniqueKey) {
                Object existing = tryGetComponent(uniqueKey);
                if (existing != null) {
                    throw new IllegalStateException(
                        String.format(
                            "Can't add multiple unique components for interface %s, attempted to add %s, but already has %s",
                            uniqueKey.interfaceType.getSimpleName(),
                            component.getClass()
                                .getSimpleName(),
                            existing
                                .getClass()
                                .getSimpleName()));
                }
            }
        }
        for (var key : keys) {
            switch (key) {
                case ComponentKey.UniqueKey<?> _ -> components.put(key, component);
                case ComponentKey.ListKey<?> listKey -> {
                    var existing = components.tryGet(listKey);
                    components.put(listKey, listKey.append(existing, component));
                }
            }
        }
        component.onAddToMachine(owner);
        if (isReady()) {
            notifyReady();
        }
    }

    private boolean removeComponentInternal(@NotNull IComponent component) {
        boolean removed = false;
        for (var key : ComponentKey.getKeysForImpl(component.getClass())) {
            switch (key) {
                case ComponentKey.UniqueKey<?> _ -> {
                    removed |= components.remove(key, component);
                }
                case ComponentKey.ListKey<?> listKey -> {
                    var current = components.tryGet(listKey);
                    if (current != null) {
                        var updated = listKey.remove(current, component);
                        if (updated != current) {
                            if (updated == null) components.remove(key);
                            else components.put(key, updated);
                            removed = true;
                        }
                    }
                }
            }
        }
        return removed;
    }

    public boolean removeComponent(@NotNull IComponent component) {
        if (!removeComponentInternal(component)) return false;
        disconnect(component);
        component.onRemoveFromMachine(owner);
        if (isReady()) {
            notifyReady();
        }

        return true;
    }

    public <T extends IComponent> boolean removeComponent(@NotNull ComponentKey.ListKey<T> key) {
        T[] targets = getComponents(key);
        if (targets.length == 0) return false;
        boolean removed = false;
        for (var target : targets) {
            if (removeComponentInternal(target)) {
                disconnect(target);
                target.onRemoveFromMachine(owner);
                removed = true;
            }
        }
        if (removed && isReady()) {
            notifyReady();
        }
        return removed;
    }

    /**
     * Always returns the same instance for a given key, so callers can safely cache the reference.
     */
    public @NotNull <T> Signal<T> signal(@NotNull SignalKey<T> key) {
        var existing = signals.tryGet(key);
        if (existing != null) return (Signal<T>) existing;
        Signal<T> signal = new Signal<>(key);
        signals.put(key, signal);
        return signal;
    }

    public @NotNull <T> T[] listeners(@NotNull SignalKey<T> key) {
        var signal = (Signal<T>) signals.tryGet(key);
        return signal != null ? signal.listeners() : key.emptyArray();
    }

    public boolean hasListeners(@NotNull SignalKey<?> key) {
        var signal = (Signal<?>) signals.tryGet(key);
        return signal != null && signal.hasListeners();
    }

    public void disconnect(@NotNull Object owner) {
        signals.forEach((signal) -> ((Signal<?>) signal).disconnect(owner));
    }

    public void disconnect() {
        signals.forEach((signal) -> ((Signal<?>) signal).disconnect());
    }
}
