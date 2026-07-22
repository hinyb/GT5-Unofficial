package gregtech.api.ec.multiblock;

import gregtech.api.ec.IEnergyOutputLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import org.jetbrains.annotations.NotNull;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoMulti;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoTunnel;

import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public interface IMultiblockEnergyOutputLogicComponent extends IEnergyOutputLogicComponent, IHatchContainerComponent {
    @Override
    default boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return addNormalDynamo(part);
    }

    @NotNull List<MTEHatchDynamo> getNormalDynamoHatches();
    @NotNull List<MTEHatch> getExoticDynamoHatches();
    boolean addNormalDynamo(@NotNull IMetaTileEntity part);
    boolean addExoticDynamo(@NotNull IMetaTileEntity part);
    boolean addLaserSource(@NotNull IMetaTileEntity part);
}
