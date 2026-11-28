package gregtech.api.ec;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.internal.BitKey;

public final class SignalKey<T> extends BitKey {

    private static final AtomicInteger ALLOCATOR = new AtomicInteger();
    private final @NotNull T[] emptyArray;

    private SignalKey(@NotNull T[] emptyArray) {
        super(ALLOCATOR);
        this.emptyArray = emptyArray;
    }

    /**
     * Warning: Be careful with generic type erasure, always assign this to an explicitly typed variable.
     */
    @SafeVarargs
    public static @NotNull <T> SignalKey<T> create(T... emptyArray) {
        return new SignalKey<>(emptyArray);
    }

    public T[] emptyArray() {
        return emptyArray;
    }
}
