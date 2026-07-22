package gregtech.common.ec.multiblock;

import gregtech.GTMod;
import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.ec.ISaveableComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IMultiblockComponent;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.ec.ITickableComponent;
import gregtech.api.ec.multiblock.IMultiblockPollutionLogicComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMuffler;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardPollutionLogicComponent implements IMultiblockPollutionLogicComponent, ITickableComponent, ISaveableComponent {
    private final List<MTEHatchMuffler> mMufflerHatches = new ArrayList<>();
    private int mPollution = 0;
    private boolean oldMufflerState = false;

    @Override
    public @NotNull List<MTEHatchMuffler> getValidHatches(){
        return filterValidMTEs(mMufflerHatches);
    }

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part) {
        if (part instanceof MTEHatchMuffler hatch) {
            if (!mMufflerHatches.contains(hatch))
            {
                mMufflerHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        setMufflers(false);
        mMufflerHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power){
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void setMufflers(boolean state) {
        oldMufflerState = state;
        final int size = mMufflerHatches.size();
        for (int i = 0; i < size; i++) {
            final MTEHatchMuffler muffler = mMufflerHatches.get(i);
            final IGregTechTileEntity tile = muffler.getBaseMetaTileEntity();
            if (tile == null || tile.isDead()) continue;
            tile.setActive(state);
        }
    }

    private void setMufflersIfChanged(boolean newState) {
        if (newState != oldMufflerState) {
            setMufflers(newState);
        }
    }

    @Override
    public int getAveragePollutionPercentage() {
        int pollutionPercent = 0;
        int mufflerCount = 0;
        for (MTEHatchMuffler muffler : getValidHatches()) {
            pollutionPercent += muffler.calculatePollutionReduction(100);
            mufflerCount++;
        }
        if (mufflerCount > 0) {
            pollutionPercent /= mufflerCount;
        } else {
            pollutionPercent = 100;
        }
        return pollutionPercent;
    }

    @Override
    public boolean polluteEnvironment(int aPollutionLevel) {
        final int VENT_AMOUNT = 10_000;
        // Early exit if pollution is disabled
        if (!GTMod.proxy.mPollution) return true;
        mPollution += aPollutionLevel;
        if (mPollution < VENT_AMOUNT) return true;
        if (mMufflerHatches.isEmpty()) {
            // No muffler present. Fail.
            return false;
        } else if (mMufflerHatches.size() == 1) {
            // One muffler, use simple method for performance.
            MTEHatchMuffler muffler = mMufflerHatches.getFirst();
            if (muffler == null || !muffler.isValid()) {
                // Muffler invalid. Fail.
                mMufflerHatches.removeFirst();
                return false;
            } else {
                // todo refactor
                if (muffler.polluteEnvironment((MTEMultiblockECBase)getMachine(), VENT_AMOUNT)) {
                    mPollution -= VENT_AMOUNT;
                } else {
                    // Muffler blocked. Fail.
                    return false;
                }
            }
        } else {
            // Multiple mufflers, split pollution output evenly between all of them.
            int mufflerCount = 0;
            int ventAmount = 0; // Allow venting of up to VENT_AMOUNT of pollution per muffler.
            for (MTEHatchMuffler _ : getValidHatches()) {
                mufflerCount++;
                if (ventAmount + VENT_AMOUNT <= mPollution) {
                    ventAmount += VENT_AMOUNT;
                }
            }
            // This might lose some small amount of pollution due to rounding, this is fine.
            ventAmount /= mufflerCount;

            for (MTEHatchMuffler muffler : getValidHatches()) {
                if (muffler.polluteEnvironment((MTEMultiblockECBase)getMachine(), ventAmount)) {
                    mPollution -= ventAmount;
                } else {
                    // Muffler blocked. Fail.
                    return false;
                }
            }
        }
        return mPollution < VENT_AMOUNT;
    }

    @Override
    public void postServerTick(IGregTechTileEntity aBaseMetaTileEntity, long tick)
    {
        setMufflersIfChanged(aBaseMetaTileEntity.isActive() && mPollution > 0);
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

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setInteger("mPollution", mPollution);
        return nbt;
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        mPollution = nbt.getInteger("mPollution");
    }
}
