package gregtech.api.ec.multiblock;

import gregtech.api.ec.IECMachine;
import gregtech.api.util.shutdown.ShutDownReason;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IECMultiblock extends IECMachine {
    void explodeMultiblock();
    void stopMachine(@NotNull ShutDownReason reason);
    @Nullable ItemStack getControllerSlot();
    void setControllerSlot(@Nullable ItemStack item);
    long getTotalRunTime();
    boolean isFormed();
}
