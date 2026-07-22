package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import org.jetbrains.annotations.NotNull;

public interface IHatchContainerComponent extends IMultiblockComponent {
    boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex);
    void detachAllHatches();
    void explodeAllHatches(long power);
}
