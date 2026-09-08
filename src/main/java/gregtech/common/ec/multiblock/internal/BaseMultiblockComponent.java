package gregtech.common.ec.multiblock.internal;

import gregtech.api.ec.IComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.util.shutdown.ShutDownReason;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class BaseMultiblockComponent implements IMultiblockComponent {
    private @Nullable IECMultiblock multiblock = null;
    @Override
    public final void onAddToMultiblock(@NotNull IECMultiblock multiblock) {
        this.multiblock = validMultiblock(multiblock);
    }

    protected IECMultiblock validMultiblock(@NotNull IECMultiblock multiblock)
    {
        return multiblock;
    }

    @Override
    public final @NotNull IECMultiblock getMachine() {
        if (multiblock == null)
        {
            throw new IllegalStateException(String.format("Component %s hasn't been attached to a machine yet.", getComponentName()));
        }
        return multiblock;
    }

    protected final void markDirty() {
        getMachine().markDirty();
    }

    protected final <T extends IComponent> @NotNull T getComponent(@NotNull Class<T> type) {
        return getMachine().getComponent(type);
    }

    protected final <T extends IComponent> @Nullable T tryGetComponent(@NotNull Class<T> type) {
        return getMachine().tryGetComponent(type);
    }

    protected final <T extends IComponent> @NotNull List<T> getComponents(@NotNull Class<T> type){
        return getMachine().getComponents(type);
    }

    protected final void stopMachine(@Nonnull ShutDownReason reason){
        getMachine().stopMachine(reason);
    }

    protected final long getTotalRuntime() {
        return getMachine().getTotalRunTime();
    }

    protected final ItemStack getControllerSlot() {
        return getMachine().getControllerSlot();
    }

    protected final boolean isMachineFormed() { return getMachine().isFormed(); }
}
