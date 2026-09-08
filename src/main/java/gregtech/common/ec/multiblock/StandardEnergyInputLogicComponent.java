package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.util.ExoticEnergyInputHelper;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardEnergyInputLogicComponent extends BaseMultiblockComponent implements IMultiblockEnergyInputLogicComponent {
    private final List<MTEHatchEnergy> mEnergyHatches = new ArrayList<>();
    private final List<MTEHatch> mExoticEnergyHatches = new ArrayList<>();
    private boolean debugEnergyPresent = false;

    @Override
    public @NotNull List<MTEHatchEnergy> getNormalEnergyHatches() {
        return filterValidMTEs(mEnergyHatches);
    }

    @Override
    public @NotNull List<MTEHatch> getExoticEnergyHatches() {
        return filterValidMTEs(mExoticEnergyHatches);
    }

    @Override
    public void detachAllHatches() {
        mEnergyHatches.clear();
        debugEnergyPresent = false;
    }

    @Override
    public void explodeAllHatches(long power){
        for (MetaTileEntity tTileEntity : getNormalEnergyHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    public boolean tryAttachEnergyHatch(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchEnergy hatch) {
            if (part instanceof MTEHatchEnergyDebug) {
                debugEnergyPresent = true;
            }
            if (!mEnergyHatches.contains(hatch))
            {
                mEnergyHatches.add(hatch);
            }
            return true;
        }
        return false;
    }
    public boolean tryAttachMultiAmpEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity)
    {
        if (aMetaTileEntity instanceof MTEHatchEnergyMulti hatch && hatch.getHatchType() == 1) {
            if (!mExoticEnergyHatches.contains(hatch))
            {
                mExoticEnergyHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    public boolean tryAttachExoticEnergyInput(@NotNull IMetaTileEntity aMetaTileEntity)
    {
        if (aMetaTileEntity instanceof MTEHatch hatch && ExoticEnergyInputHelper.isExoticEnergyInput(aMetaTileEntity)) {
            if (!mExoticEnergyHatches.contains(hatch))
            {
                mExoticEnergyHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isDebugEnergyPresent() {
        return debugEnergyPresent;
    }

    @Override
    public long getMaxInputVoltage() {
        return ExoticEnergyInputHelper.getMaxInputVoltageMulti(mEnergyHatches);
    }

    @Override
    public long getAverageInputVoltage() {
        return ExoticEnergyInputHelper.getAverageInputVoltageMulti(mEnergyHatches);
    }

    @Override
    public long getMaxInputAmps() {
        return ExoticEnergyInputHelper.getMaxWorkingInputAmpsMulti(mEnergyHatches);
    }

    @Override
    public long getMaxInputEu() {
        return ExoticEnergyInputHelper.getTotalEuMulti(mEnergyHatches);
    }

    @Override
    public long getMaxInputPower() {
        return ExoticEnergyInputHelper.getMaxInputPower(mEnergyHatches);
    }

    @Override
    public long getInputVoltageTier() {
        return ExoticEnergyInputHelper.getInputVoltageTier(mEnergyHatches);
    }

    @Override
    public boolean drainEnergyInput(long aEU) {
        return ExoticEnergyInputHelper.drainEnergy(aEU, getNormalEnergyHatches());
    }

    @TestOnly
    public void setEnergyHatches(ArrayList<MTEHatchEnergy> EnergyHatches) {
        mEnergyHatches.clear();
        mEnergyHatches.addAll(EnergyHatches);
    }

    @TestOnly
    public void setExoticEnergyHatches(List<MTEHatch> ExoticEnergyHatches) {
        mExoticEnergyHatches.clear();
        mExoticEnergyHatches.addAll(ExoticEnergyHatches);
    }
}
