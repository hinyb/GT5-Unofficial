package gregtech.api.ec.internal;

import java.util.Arrays;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.GTValues;

public final class BitIndexedArray<T extends BitKey> {

    private long[] mask = GTValues.emptyLongArray;
    private Object[] entries = GTValues.emptyObjectArray;

    public boolean contains(@NotNull T key) {
        int w = key.wordIndex;
        if (w >= mask.length) return false;
        return (mask[w] & key.bitMask) != 0;
    }

    public @Nullable Object tryGet(@NotNull T key) {
        int index = indexOf(key);
        return index != -1 ? entries[index] : null;
    }

    public void put(@NotNull T key, @NotNull Object value) {
        int index = indexOf(key);
        if (index != -1) {
            entries[index] = value;
        } else {
            addEntry(key, value);
        }
    }

    public boolean remove(@NotNull T key) {
        int index = indexOf(key);
        if (index != -1) {
            removeEntry(key, index);
            return true;
        }
        return false;
    }

    public boolean remove(@NotNull T key, @NotNull Object target) {
        int index = indexOf(key);
        if (index != -1 && entries[index] == target) {
            removeEntry(key, index);
            return true;
        }
        return false;
    }

    public void forEach(@NotNull Consumer<Object> consumer) {
        for (var entry : entries) {
            consumer.accept(entry);
        }
    }

    private int indexOf(@NotNull T key) {
        int w = key.wordIndex;
        if (w >= mask.length) return -1;
        long word = mask[w];
        if ((word & key.bitMask) == 0) return -1;
        int index = Long.bitCount(word & key.lowerMask);
        for (int i = 0; i < w; i++) {
            index += Long.bitCount(mask[i]);
        }
        return index;
    }

    private void addEntry(@NotNull T key, @NotNull Object value) {
        int w = key.wordIndex;
        int denseIndex = 0;
        if (w >= mask.length) {
            mask = Arrays.copyOf(mask, w + 1);
        } else {
            denseIndex += Long.bitCount(mask[w] & key.lowerMask);
        }
        for (int i = 0; i < w; i++) {
            denseIndex += Long.bitCount(mask[i]);
        }
        mask[w] |= key.bitMask;
        var updated = new Object[entries.length + 1];
        if (denseIndex > 0) {
            System.arraycopy(entries, 0, updated, 0, denseIndex);
        }
        updated[denseIndex] = value;
        if (denseIndex < entries.length) {
            System.arraycopy(entries, denseIndex, updated, denseIndex + 1, entries.length - denseIndex);
        }
        entries = updated;
    }

    private void removeEntry(@NotNull T key, int denseIndex) {
        int w = key.wordIndex;
        mask[w] &= ~key.bitMask;
        if (entries.length == 1) {
            entries = GTValues.emptyObjectArray;
            return;
        }
        var updated = new Object[entries.length - 1];
        if (denseIndex > 0) {
            System.arraycopy(entries, 0, updated, 0, denseIndex);
        }
        if (denseIndex < entries.length - 1) {
            System.arraycopy(entries, denseIndex + 1, updated, denseIndex, entries.length - denseIndex - 1);
        }
        entries = updated;
    }
}
