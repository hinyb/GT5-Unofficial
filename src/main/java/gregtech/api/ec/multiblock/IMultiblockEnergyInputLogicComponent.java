package gregtech.api.ec.multiblock;

import gregtech.api.ec.IEnergyInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMultiblockEnergyInputLogicComponent extends IEnergyInputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachEnergyHatch(part);
    }
    @NotNull List<MTEHatchEnergy> getNormalEnergyHatches();
    @NotNull List<MTEHatch> getExoticEnergyHatches();
    boolean tryAttachEnergyHatch(@NotNull IMetaTileEntity part);
    boolean tryAttachMultiAmpEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity);
    boolean tryAttachExoticEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity);
    boolean isDebugEnergyPresent();
}
