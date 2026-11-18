package gregtech.common.ec.multiblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;

import gregtech.api.ec.IMachineModeComponent;
import gregtech.api.ec.IOnScrewdriverClickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IWailaDataComponent;
import gregtech.api.ec.multiblock.IRecipeLockingComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class MachineModeComponent extends BaseMultiblockComponent
    implements IMachineModeComponent, IOnScrewdriverClickComponent, IWailaDataComponent, ISavableComponent {

    @Override
    public void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        tag.setString("mode", getMachineModeName());
    }

    @FunctionalInterface
    public interface ScrewdriverClickCallback {

        boolean onRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ, ItemStack aTool);
    }

    private int machineMode = 0;
    private final int modeCount;
    private final List<UITexture> machineModeIcons = new ArrayList<>(4);
    private final @NotNull IntFunction<String> modeKeyProvider;
    private final @NotNull IntUnaryOperator nextModeOperator;
    private @NotNull Pos2d buttonPos = new Pos2d(80, 91);
    private @Nullable ScrewdriverClickCallback onScrewdriverRightClick = null;
    private @Nullable Runnable onModeSwitchClickCallback = null;

    public MachineModeComponent(@NotNull String... modeKeys) {
        int modeCount = modeKeys.length;
        if (modeCount == 0) {
            throw new IllegalArgumentException("MachineModeComponent must have at least one mode!");
        }
        this(
            modeCount,
            index -> (index >= 0 && index < modeKeys.length) ? modeKeys[index] : "GT5U.MULTI_MACHINE_MODE.unknown",
            mode -> (mode + 1) % modeCount);
    }

    public MachineModeComponent(int modeCount, @NotNull IntFunction<String> modeKeyProvider,
        @NotNull IntUnaryOperator nextModeOperator) {
        this.modeCount = modeCount;
        this.modeKeyProvider = modeKeyProvider;
        this.nextModeOperator = nextModeOperator;
    }

    @Override
    public int getMachineModeCount() {
        return modeCount;
    }

    @Override
    public int getMachineMode() {
        return machineMode;
    }

    @Override
    public void setMachineMode(int index) {
        int normalized = Math.floorMod(index, modeCount);
        if (this.machineMode == normalized) return;
        machineMode = normalized;

        onMachineModeSwitchClick();
        var recipeLocking = tryGetComponent(IRecipeLockingComponent.KEY);
        if (recipeLocking != null) {
            recipeLocking.setSingleRecipeCheck(null);
        }
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            recipeLogic.scheduleRecipeCheck(RecipeCheckReason.IMMEDIATE);
        }
    }

    @Override
    public int nextMachineMode() {
        return nextModeOperator.applyAsInt(machineMode);
    }

    @Override
    public @Nullable UITexture getMachineModeIcon(int index) {
        if (index < 0 || index >= machineModeIcons.size()) return null;
        return machineModeIcons.get(index);
    }

    @Override
    public @NotNull MachineModeComponent addMachineModeIcons(@NotNull UITexture... icons) {
        Collections.addAll(machineModeIcons, icons);
        return this;
    }

    @Override
    public @NotNull String getMachineModeKey(int index) {
        return modeKeyProvider.apply(index);
    }

    @Override
    public @NotNull Pos2d getMachineModeSwitchButtonPos() {
        return buttonPos;
    }

    @Override
    public void onMachineModeSwitchClick() {
        if (onModeSwitchClickCallback != null) onModeSwitchClickCallback.run();
    }

    @Override
    public @NotNull String getSaveKey() {
        return "mode";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setInteger("machineMode", machineMode);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        if (nbt.hasKey("machineMode")) {
            machineMode = nbt.getInteger("machineMode");
        }
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        loadLegacyNBTData(nbt);
    }

    @Override
    public boolean onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        if (onScrewdriverRightClick != null) {
            return onScrewdriverRightClick.onRightClick(side, aPlayer, aX, aY, aZ, aTool);
        }
        return false;
    }

    public @NotNull MachineModeComponent setOnScrewdriverClick(@NotNull ScrewdriverClickCallback callback) {
        onScrewdriverRightClick = callback;
        return this;
    }

    public @NotNull MachineModeComponent setButtonPos(@NotNull Pos2d pos) {
        buttonPos = pos;
        return this;
    }

    public @NotNull MachineModeComponent setOnModeSwitchClick(@NotNull Runnable callback) {
        onModeSwitchClickCallback = callback;
        return this;
    }
}
