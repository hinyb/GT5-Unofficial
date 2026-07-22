package gregtech.api.util;

import static gregtech.api.util.GTUtility.uncheckedValidMTEList;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyTunnel;

public class ExoticEnergyInputHelper {

    /**
     * The Valid Types of TecTech Hatch List.
     */
    private static final List<Class<? extends MTEHatch>> sExoticEnergyHatchType = new ArrayList<>();

    static {
        register(MTEHatchEnergyMulti.class);
        register(MTEHatchEnergyTunnel.class);
    }

    public static void register(Class<? extends MTEHatch> clazz) {
        if (!MTEHatch.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException(clazz.getName() + " is not a subclass of " + MTEHatch.class.getName());
        sExoticEnergyHatchType.add(clazz);
    }

    public static boolean drainEnergy(long aEU, Iterable<? extends MTEHatch> hatches) {
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            var mte = tHatch.getBaseMetaTileEntity();
            long tDrain = Math.min(mte.getStoredEU(), aEU);
            mte.decreaseStoredEnergyUnits(tDrain, false);
            aEU -= tDrain;
        }
        return aEU <= 0;
    }

    public boolean drainEnergyInput(long aEU, Iterable<? extends MTEHatch> hatches) {
        if (aEU <= 0) return true;

        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            var mte = tHatch.getBaseMetaTileEntity();
            long tDrain = Math.min(mte.getStoredEU(),aEU);
            mte.decreaseStoredEnergyUnits(tDrain, false);
            // basicly copied from ExoticEnergyInputHelper, makes machine use all hatches for power
            aEU -= tDrain;

            if (aEU <= 0) return true;
        }

        return false;
    }

    public static boolean isExoticEnergyInput(IMetaTileEntity aHatch) {
        for (Class<?> clazz : sExoticEnergyHatchType) {
            if (clazz.isInstance(aHatch)) return true;
        }
        return false;
    }

    public static long getTotalEuMulti(Iterable<? extends MTEHatch> hatches) {
        long rEU = 0L;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            rEU += tHatch.getBaseMetaTileEntity().getInputVoltage() * tHatch.maxWorkingAmperesIn();
        }
        return rEU;
    }

    public static long getMaxInputPower(Iterable<? extends MTEHatch> hatches)
    {
        long eut = 0;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            IGregTechTileEntity baseTile = tHatch.getBaseMetaTileEntity();
            eut += baseTile.getInputVoltage() * baseTile.getInputAmperage();
        }
        return eut;
    }

    public static long getInputVoltageTier(Iterable<? extends MTEHatch> hatches)
    {
        var it = uncheckedValidMTEList(hatches).iterator();
        if (!it.hasNext())
        {
            return 0;
        }
        long rTier = it.next().getInputTier();
        while (it.hasNext())
        {
            if (it.next().getInputTier() != rTier) return 0;
        }
        return rTier;
    }

    public static long getMaxInputVoltageMulti(Iterable<? extends MTEHatch> hatches) {
        long rVoltage = 0;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            rVoltage += tHatch.getBaseMetaTileEntity()
                .getInputVoltage();
        }
        return rVoltage;
    }

    public static long getAverageInputVoltageMulti(Iterable<? extends MTEHatch> hatches) {
        long rVoltage = 0;
        long size = 0;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            rVoltage += tHatch.getBaseMetaTileEntity().getInputVoltage();
            size++;
        }
        return size == 0 ? 0 : rVoltage / size;
    }

    public static long getMaxInputAmpsMulti(Iterable<? extends MTEHatch> hatches) {
        long rAmp = 0;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            rAmp += tHatch.getBaseMetaTileEntity().getInputAmperage();
        }
        return rAmp;
    }

    public static long getMaxWorkingInputAmpsMulti(Iterable<? extends MTEHatch> hatches) {
        long rAmp = 0;
        for (MTEHatch tHatch : uncheckedValidMTEList(hatches)) {
            rAmp += tHatch.maxWorkingAmperesIn();
        }
        return rAmp;
    }

    public static List<Class<? extends MTEHatch>> getAllClasses() {
        return Collections.unmodifiableList(sExoticEnergyHatchType);
    }
}
