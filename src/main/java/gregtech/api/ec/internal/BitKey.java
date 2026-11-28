package gregtech.api.ec.internal;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

public class BitKey {

    final int wordIndex;
    final long bitMask;
    final long lowerMask;

    protected BitKey(@NotNull AtomicInteger allocator) {
        int id = allocator.getAndIncrement();
        this.wordIndex = id >> 6;
        this.bitMask = 1L << (id & 63);
        this.lowerMask = this.bitMask - 1;
    }
}
