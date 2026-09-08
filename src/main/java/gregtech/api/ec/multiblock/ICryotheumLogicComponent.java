package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICryotheumLogicComponent extends IHatchContainerComponent, IUniqueComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachCryotheum(part);
    }
    boolean tryAttachCryotheum(@NotNull IMetaTileEntity part);
    @NotNull List<MTEHatch> getValidHatches();
}
