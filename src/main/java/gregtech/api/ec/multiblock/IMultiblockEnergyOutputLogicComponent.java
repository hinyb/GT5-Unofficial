package gregtech.api.ec.multiblock;

import gregtech.api.ec.IEnergyOutputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMultiblockEnergyOutputLogicComponent extends IEnergyOutputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachNormalDynamo(part);
    }

    @NotNull List<MTEHatchDynamo> getNormalDynamoHatches();
    @NotNull List<MTEHatch> getExoticDynamoHatches();
    boolean tryAttachNormalDynamo(@NotNull IMetaTileEntity part);
    boolean tryAttachExoticDynamo(@NotNull IMetaTileEntity part);
    boolean tryAttachLaserSource(@NotNull IMetaTileEntity part);
}
