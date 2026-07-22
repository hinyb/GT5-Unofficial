package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;
import gregtech.api.ec.IECMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockComponent extends IComponent {
    @Override
    default void onAddToMachine(@NotNull IECMachine machine)
    {
        if (machine instanceof IECMultiblock multiblock)
        {
            onAddToMultiblock(multiblock);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    void onAddToMultiblock(@NotNull IECMultiblock multiblock);

    @Override
    @Nullable IECMultiblock getMachine();
}
