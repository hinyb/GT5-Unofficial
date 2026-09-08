package gregtech.common.ec.multiblock;

import gregtech.api.ec.IOnRecipeFinishComponent;
import gregtech.api.ec.IOnRecipeTickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IServerTickableComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IPostStructureCheckComponent;
import gregtech.api.ec.multiblock.IStructureLogicComponent;
import gregtech.api.enums.MaintenanceIssues;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.multi.drone.MTEDroneCentre;
import gregtech.common.tileentities.machines.multi.drone.MTEHatchDroneDownLink;
import gregtech.common.tileentities.machines.multi.drone.production.ProductionRecord;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static gregtech.api.util.GTUtility.filterValidMTEs;

public class StandardMaintenanceLogicComponent extends BaseMultiblockComponent implements IMaintenanceLogicComponent, ISavableComponent, IOnRecipeTickComponent, IOnRecipeFinishComponent, IServerTickableComponent {
    private final List<MTEHatchMaintenance> mMaintenanceHatches = new ArrayList<>();
    private @Nullable MTEHatchDroneDownLink ddl = null;
    private int state = 0;
    private final int allowMasks;
    private final int errorAmount;

    public StandardMaintenanceLogicComponent(int allowMasks) {
        if (allowMasks == 0) {
            throw new IllegalArgumentException("allowMasks can't be 0");
        }
        this.allowMasks = allowMasks;
        this.errorAmount = Integer.bitCount(allowMasks);
    }

    @Override
    public boolean tryAttachHatch(@NotNull IMetaTileEntity part, int aBaseCasingIndex) {
        return tryAttachMaintenance(part);
    }

    public boolean tryAttachMaintenance(@NotNull IMetaTileEntity part) {
        if (part instanceof MTEHatchMaintenance hatch) {
            if (hatch instanceof MTEHatchDroneDownLink droneDownLink) {
                // todo refactor
                droneDownLink.registerMachineController((MTEMultiBlockBase) getMachine());
                ddl = droneDownLink;
            }
            if (!mMaintenanceHatches.contains(hatch)) {
                mMaintenanceHatches.add(hatch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void detachAllHatches() {
        mMaintenanceHatches.clear();
    }

    @Override
    public void explodeAllHatches(long power) {
        for (MetaTileEntity tTileEntity : getValidHatches()) {
            tTileEntity.getBaseMetaTileEntity().doExplosion(power);
        }
    }

    @Override
    public @NotNull List<MTEHatchMaintenance> getValidHatches() {
        return filterValidMTEs(mMaintenanceHatches);
    }

    @Override
    public @Nullable MTEHatchDroneDownLink tryGetDroneDownLink() {
        return ddl;
    }

    @Override
    public boolean hasIssue(int mask) {
        return (state & mask) != 0;
    }

    @Override
    public void setIssue(int mask, boolean hasIssue) {
        mask &= allowMasks;
        if (mask == 0) {
            throw new IllegalArgumentException("Unsupported mask");
        }
        if (hasIssue) {
            state |= mask;
        } else {
            state &= ~mask;
        }
    }

    @Override
    public int getMaintenanceErrorBits() {
        return state;
    }

    @Override
    public int getRepairStatus() {
        return errorAmount - Integer.bitCount(state);
    }

    @Override
    public int getIdealStatus() {
        return errorAmount;
    }

    @Override
    public void fixAllIssues() {
        state = 0;
    }

    @Override
    public void causeAllIssues() {
        state = allowMasks;
    }

    @Override
    public void causeRandomIssue() {
        int index = ThreadLocalRandom.current().nextInt(errorAmount);
        int mask = allowMasks;
        for (int i = 0; i < index; i++) {
            mask &= mask - 1;
        }
        mask = Integer.lowestOneBit(mask);
        state |= mask;
    }

    @Override
    public void checkMaintenance() {
        if (state == 0) return;
        for (MTEHatchMaintenance tHatch : mMaintenanceHatches) {
            boolean tDidRepair = false;

            if (tHatch.mAuto) tDidRepair = tHatch.autoMaintainance();

            // For each tool, only if needed, collect the tool flags
            // that this Maintenance Hatch has provided
            if (tHatch.mWrench && hasIssue(MaintenanceIssues.WRENCH)) {
                setIssue(MaintenanceIssues.WRENCH, false);
                tDidRepair = true;
            }
            if (tHatch.mScrewdriver && hasIssue(MaintenanceIssues.SCREWDRIVER)) {
                setIssue(MaintenanceIssues.SCREWDRIVER, false);
                tDidRepair = true;
            }
            if (tHatch.mSoftMallet && hasIssue(MaintenanceIssues.SOFT_MALLET)) {
                setIssue(MaintenanceIssues.SOFT_MALLET, false);
                tDidRepair = true;
            }
            if (tHatch.mHardHammer && hasIssue(MaintenanceIssues.HARD_HAMMER)) {
                setIssue(MaintenanceIssues.HARD_HAMMER, false);
                tDidRepair = true;
            }
            if (tHatch.mSolderingTool && hasIssue(MaintenanceIssues.SOLDERING_TOOL)) {
                setIssue(MaintenanceIssues.SOLDERING_TOOL, false);
                tDidRepair = true;
            }
            if (tHatch.mCrowbar && hasIssue(MaintenanceIssues.CROWBAR)) {
                setIssue(MaintenanceIssues.CROWBAR, false);
                tDidRepair = true;
            }

            tHatch.mWrench = false;
            tHatch.mScrewdriver = false;
            tHatch.mSoftMallet = false;
            tHatch.mHardHammer = false;
            tHatch.mSolderingTool = false;
            tHatch.mCrowbar = false;

            // todo refactor
            if (tDidRepair) tHatch.onMaintenancePerformed((MTEMultiBlockBase) getMachine());
        }
    }

    @Override
    public int getMaintenanceEfficiencyPenalty() {
        return (getIdealStatus() - getRepairStatus()) * 1000;
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setInteger("state", state);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound aNBT) {
        setIssue(MaintenanceIssues.WRENCH, aNBT.getBoolean("mWrench"));
        setIssue(MaintenanceIssues.SCREWDRIVER, aNBT.getBoolean("mScrewdriver"));
        setIssue(MaintenanceIssues.SOFT_MALLET, aNBT.getBoolean("mSoftMallet") || aNBT.getBoolean("mSoftHammer"));
        setIssue(MaintenanceIssues.HARD_HAMMER, aNBT.getBoolean("mHardHammer"));
        setIssue(MaintenanceIssues.SOLDERING_TOOL, aNBT.getBoolean("mSolderingTool"));
        setIssue(MaintenanceIssues.CROWBAR, aNBT.getBoolean("mCrowbar"));
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        state = nbt.getInteger("state") & allowMasks;
    }

    @Override
    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (getRepairStatus() == 0) {
            stopMachine(ShutDownReasonRegistry.NO_REPAIR);
            return false;
        }
        if (recipeLogicComponent.getRuntime() == 1000) {
            if (getMachine().getRandomNumber(6000) == 0) {
                causeRandomIssue();
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeTickPriority getRecipeTickPriority() {
        return RecipeTickPriority.MAINTAIN_ISSUE;
    }

    @Override
    public void onRecipeFinish(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (ddl == null) return;
        MTEDroneCentre centre = ddl.getCentre();
        if (centre != null) {
            ProductionRecord pdr = centre.productionDataRecorder;
            if (pdr.isActive())
                pdr.addRecord(((long) recipeLogicComponent.getMaxProgressTime()) * recipeLogicComponent.getEUt(), recipeLogicComponent.getOutputItems(), recipeLogicComponent.getOutputFluids());
        }
    }

    @Override
    public @NotNull RecipeFinishPriority getRecipeFinishPriority() {
        return RecipeFinishPriority.DRONE_COLLECT_DATA;
    }

    @Override
    public void postServerTick(IGregTechTileEntity te, long aTick) {
        if (isMachineFormed()) {
            checkMaintenance();
            if (getRepairStatus() <= 0 && te.isAllowedToWork()) {
                stopMachine(ShutDownReasonRegistry.NO_REPAIR);
            }
        }
    }

    @Override
    public @NotNull ServerTickPriority getServerTickPriority() {
        return ServerTickPriority.MAINTAIN;
    }
}
