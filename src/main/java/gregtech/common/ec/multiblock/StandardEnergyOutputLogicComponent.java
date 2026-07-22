package gregtech.common.ec.multiblock;

import com.google.common.collect.Iterables;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyOutputLogicComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoMulti;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoTunnel;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.*;
import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardEnergyOutputLogicComponent implements IMultiblockEnergyOutputLogicComponent {
    private final List<MTEHatchDynamo> mDynamoHatches = new ArrayList<>();
    private final List<MTEHatch> mExoticDynamoHatches = new ArrayList<>();
    private final boolean explodeOnFailure;
    private final boolean strictCapacityLimit;
    StandardEnergyOutputLogicComponent(boolean explodeOnFailure, boolean strictCapacityLimit)
    {
        this.explodeOnFailure = explodeOnFailure;
        this.strictCapacityLimit = strictCapacityLimit;
    }

    @Override
    public boolean addEnergyOutputMultipleDynamos(long aEU, boolean aAllowMixedVoltageDynamos) {
        if (aEU <= 0) return true;
        if (mDynamoHatches.isEmpty() && mExoticDynamoHatches.isEmpty()) {
            return false;
        }
        var allDynamos = Iterables.concat(validMTEList(mDynamoHatches), validMTEList(mExoticDynamoHatches));
        long injected = 0;
        long totalOutput = 0;
        long aFirstVoltageFound = -1;
        boolean aFoundMixedDynamos = false;
        for (MTEHatch aDynamo : allDynamos) {
            long aVoltage = aDynamo.maxEUOutput();
            long aTotal = aDynamo.maxAmperesOut() * aVoltage;
            // Check against voltage to check when hatch mixing
            if (aFirstVoltageFound == -1) {
                aFirstVoltageFound = aVoltage;
            } else {
                if (aFirstVoltageFound != aVoltage) {
                    aFoundMixedDynamos = true;
                }
            }
            totalOutput += aTotal;
        }

        if (totalOutput < aEU || (aFoundMixedDynamos && !aAllowMixedVoltageDynamos)) {
            if (explodeOnFailure && this.multiblock != null) {
                multiblock.explodeMultiblock();
            }
            return false;
        }

        long leftToInject;
        long aVoltage;
        int aAmpsToInject;
        int aRemainder;
        int ampsOnCurrentHatch;
        for (MTEHatch aDynamo : allDynamos) {
            leftToInject = aEU - injected;
            aVoltage = aDynamo.maxEUOutput();
            aAmpsToInject = (int) (leftToInject / aVoltage);
            aRemainder = (int) (leftToInject - (aAmpsToInject * aVoltage));
            ampsOnCurrentHatch = (int) Math.min(aDynamo.maxAmperesOut(), aAmpsToInject);
            var mte = aDynamo.getBaseMetaTileEntity();
            if (strictCapacityLimit) {
                for (int i = 0; i < ampsOnCurrentHatch; i++) {
                    mte.increaseStoredEnergyUnits(aVoltage, false);
                }
            } else{
                mte.increaseStoredEnergyUnits(aVoltage * ampsOnCurrentHatch, false);
            }
            injected += aVoltage * ampsOnCurrentHatch;
            if (aRemainder > 0 && ampsOnCurrentHatch < aDynamo.maxAmperesOut()) {
                mte.increaseStoredEnergyUnits(aRemainder, false);
                injected += aRemainder;
            }
        }
        return injected > 0;
    }

    @Override
    public @NotNull List<MTEHatchDynamo> getNormalDynamoHatches() {
        return filterValidMTEs(mDynamoHatches);
    }

    @Override
    public @NotNull List<MTEHatch> getExoticDynamoHatches() {
        return filterValidMTEs(mExoticDynamoHatches);
    }

    @Override
    public boolean addNormalDynamo(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchDynamo hatch && hatch.maxAmperesOut() <= 4) {
            if (!mDynamoHatches.contains(hatch))
            {
                mDynamoHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean addExoticDynamo(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchDynamoMulti hatch) {
            if (!mExoticDynamoHatches.contains(hatch))
            {
                mExoticDynamoHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean addLaserSource(@NotNull IMetaTileEntity part)
    {
        if (part instanceof MTEHatchDynamoTunnel hatch) {
            if (!mExoticDynamoHatches.contains(hatch))
            {
                mExoticDynamoHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        if (part instanceof MTEHatchDynamo hatch) {
            if (!mDynamoHatches.contains(hatch))
            {
                mDynamoHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mDynamoHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
        for (MetaTileEntity tTileEntity : getNormalDynamoHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    private @Nullable IECMultiblock multiblock = null;
    @Override
    public void onAddToMultiblock(@NotNull IECMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public @Nullable IECMultiblock getMachine() {
        return multiblock;
    }
}
