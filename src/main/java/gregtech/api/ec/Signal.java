package gregtech.api.ec;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.GTValues;

public final class Signal<T> {

    @FunctionalInterface
    public interface ActiveListener<T> {

        void onActiveChanged(@NotNull Signal<T> signal, boolean active);
    }

    private static final ActiveListener<?>[] EMPTY_WATCHERS = new ActiveListener<?>[0];

    private final @NotNull T[] emptyArray;
    private @NotNull T[] listeners;
    private @NotNull Object[] owners = GTValues.emptyObjectArray;
    private int[] priorities = GTValues.emptyIntArray;
    private @Nullable Signal<ActiveListener<T>> activeSignal;
    private boolean notifyDirty = false;

    public Signal(@NotNull SignalKey<T> key) {
        this(key.emptyArray());
    }

    public Signal(@NotNull T[] emptyArray) {
        this.emptyArray = emptyArray;
        listeners = emptyArray;
    }

    public @NotNull T[] listeners() {
        return listeners;
    }

    public boolean hasListeners() {
        return listeners.length > 0;
    }

    public @NotNull Connection connect(@NotNull Object owner, int priority, @NotNull T listener) {
        int len = listeners.length;
        int insertIndex = len;
        boolean hasPriority = priorities != GTValues.emptyIntArray;
        if (len > 0) {
            if (hasPriority) {
                if (priorities[len - 1] > priority) {
                    for (int i = 0; i < len; i++) {
                        if (priorities[i] > priority) {
                            insertIndex = i;
                            break;
                        }
                    }
                }
            } else if (priority < 0) {
                insertIndex = 0;
            }
        }
        var newListeners = Arrays.copyOf(listeners, len + 1);
        var newOwners = Arrays.copyOf(owners, len + 1);
        newListeners[insertIndex] = listener;
        newOwners[insertIndex] = owner;
        if (insertIndex < len) {
            System.arraycopy(listeners, insertIndex, newListeners, insertIndex + 1, len - insertIndex);
            System.arraycopy(owners, insertIndex, newOwners, insertIndex + 1, len - insertIndex);
        }
        if (hasPriority) {
            var newPriorities = Arrays.copyOf(priorities, len + 1);
            newPriorities[insertIndex] = priority;
            if (insertIndex < len) {
                System.arraycopy(priorities, insertIndex, newPriorities, insertIndex + 1, len - insertIndex);
            }
            priorities = newPriorities;
        } else if (priority != 0) {
            var newPriorities = new int[len + 1];
            newPriorities[insertIndex] = priority;
            priorities = newPriorities;
        }
        listeners = newListeners;
        owners = newOwners;
        if (len == 0) {
            notifyActive();
        }
        return new ConnectionImpl<>(this, owner, priority, listener);
    }

    public @NotNull Connection watchActive(@NotNull Object owner, int priority, @NotNull ActiveListener<T> listener) {
        if (activeSignal == null) {
            activeSignal = new Signal<>((ActiveListener<T>[]) EMPTY_WATCHERS);
        }
        return activeSignal.connect(owner, priority, listener);
    }

    private void disconnectListener(@NotNull Object owner, int priority, @NotNull T target) {
        int len = listeners.length;
        int removeIndex = -1;
        boolean hasPriority = priorities != GTValues.emptyIntArray;
        for (int i = 0; i < len; i++) {
            if (owners[i] == owner && (!hasPriority || priorities[i] == priority) && listeners[i] == target) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex == -1) return;
        int remainingLen = len - 1;
        if (remainingLen == 0) {
            listeners = emptyArray;
            owners = GTValues.emptyObjectArray;
            priorities = GTValues.emptyIntArray;
            notifyActive();
            return;
        }
        var newListeners = Arrays.copyOf(listeners, remainingLen);
        var newOwners = Arrays.copyOf(owners, remainingLen);
        if (removeIndex < remainingLen) {
            System.arraycopy(listeners, removeIndex + 1, newListeners, removeIndex, remainingLen - removeIndex);
            System.arraycopy(owners, removeIndex + 1, newOwners, removeIndex, remainingLen - removeIndex);
        }
        if (hasPriority) {
            var newPriorities = Arrays.copyOf(priorities, remainingLen);
            if (removeIndex < remainingLen) {
                System.arraycopy(priorities, removeIndex + 1, newPriorities, removeIndex, remainingLen - removeIndex);
            }
            priorities = newPriorities;
        }
        listeners = newListeners;
        owners = newOwners;
    }

    public void disconnectWatchers(@NotNull Object owner) {
        if (activeSignal != null) {
            activeSignal.disconnect(owner);
        }
    }

    public void disconnectListeners(@NotNull Object owner) {
        int len = listeners.length;
        int remainingLen = 0;
        for (int i = 0; i < len; i++) {
            if (owners[i] != owner) remainingLen++;
        }
        if (remainingLen == len) return;
        if (remainingLen == 0) {
            disconnectListeners();
            return;
        }
        boolean hasPriority = priorities != GTValues.emptyIntArray;
        var newListeners = Arrays.copyOf(emptyArray, remainingLen);
        var newOwners = new Object[remainingLen];
        var newPriorities = hasPriority ? new int[remainingLen] : GTValues.emptyIntArray;
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (owners[i] != owner) {
                newListeners[index] = listeners[i];
                newOwners[index] = owners[i];
                if (hasPriority) {
                    newPriorities[index] = priorities[i];
                }
                index++;
            }
        }
        listeners = newListeners;
        owners = newOwners;
        priorities = newPriorities;
    }

    public void disconnect(@NotNull Object owner) {
        disconnectWatchers(owner);
        disconnectListeners(owner);
    }

    public void disconnectWatchers() {
        if (activeSignal != null) {
            activeSignal.disconnect();
        }
    }

    public void disconnectListeners() {
        if (listeners.length == 0) return;
        listeners = emptyArray;
        owners = GTValues.emptyObjectArray;
        priorities = GTValues.emptyIntArray;
        notifyActive();
    }

    public void disconnect() {
        disconnectWatchers();
        disconnectListeners();
    }

    private void notifyActive() {
        if (activeSignal == null || notifyDirty) return;
        notifyDirty = true;
        boolean active = listeners.length > 0;
        while (true) {
            for (var watcher : activeSignal.listeners()) {
                watcher.onActiveChanged(this, active);
            }
            boolean next = listeners.length > 0;
            if (active == next) break;
            active = next;
        }
        notifyDirty = false;
    }

    private static final class ConnectionImpl<T> implements Connection {

        private Signal<T> signal;
        private Object owner;
        private final int priority;
        private T listener;

        ConnectionImpl(@NotNull Signal<T> signal, @NotNull Object owner, int priority, @NotNull T listener) {
            this.signal = signal;
            this.owner = owner;
            this.priority = priority;
            this.listener = listener;
        }

        @Override
        public void disconnect() {
            if (signal == null) return;
            var s = signal;
            var o = owner;
            var l = listener;
            signal = null;
            owner = null;
            listener = null;
            s.disconnectListener(o, priority, l);
        }
    }
}
