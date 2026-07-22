package gregtech.api.ec.multiblock;

import gregtech.api.ec.IEnergyInputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.util.ExoticEnergyInputHelper;
import org.jetbrains.annotations.NotNull;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;

import java.util.List;

public interface IMultiblockEnergyInputLogicComponent extends IEnergyInputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return addEnergyHatch(part);
    }
    @NotNull List<MTEHatchEnergy> getNormalEnergyHatches();
    @NotNull List<MTEHatch> getExoticEnergyHatches();
    boolean addEnergyHatch(@NotNull IMetaTileEntity part);
    boolean addMultiAmpEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity);
    boolean addExoticEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity);
    boolean isDebugEnergyPresent();
}
