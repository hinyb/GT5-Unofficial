package gregtech.common.ec.multiblock;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.ec.multiblock.IVoidingConfigComponent;
import gregtech.api.enums.VoidingMode;
import gregtech.api.logic.ProcessingLogic;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.tileentities.machines.RecipeCheckReason;

public class VoidingConfigComponent extends BaseMultiblockComponent
    implements IVoidingConfigComponent, IProcessingLogicSetupComponent, ISavableComponent {

    private VoidingMode voidingMode = VoidingMode.VOID_NONE;
    private Set<VoidingMode> allowedVoidingModes = VoidingMode.ALL_OPTIONS;

    protected static final String VOID_EXCESS_NBT_KEY = "voidExcess";
    protected static final String VOIDING_MODE_NBT_KEY = "voidingMode";

    @Override
    public Set<VoidingMode> getAllowedVoidingModes() {
        return allowedVoidingModes;
    }

    public VoidingConfigComponent setAllowedVoidingModes(Set<VoidingMode> allowedVoidingModes) {
        this.allowedVoidingModes = allowedVoidingModes;
        return this;
    }

    @Override
    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    @Override
    public void setVoidingMode(VoidingMode mode) {
        voidingMode = mode;
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            recipeLogic.scheduleRecipeCheck(RecipeCheckReason.IMMEDIATE);
        }
    }

    @Override
    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setVoidProtection(protectsExcessItem(), protectsExcessFluid());
    }

    @Override
    public @NotNull String getSaveKey() {
        return "voiding";
    }

    @Override
    public @Nullable NBTTagCompound saveComponentData() {
        var nbt = new NBTTagCompound();
        nbt.setString(VOIDING_MODE_NBT_KEY, voidingMode.name);
        return nbt;
    }

    @Override
    public void loadLegacyNBTData(@NotNull NBTTagCompound nbt) {
        VoidingMode mode = null;
        if (nbt.hasKey(VOIDING_MODE_NBT_KEY, Constants.NBT.TAG_STRING)) {
            mode = VoidingMode.fromName(nbt.getString(VOIDING_MODE_NBT_KEY));
        } else if (nbt.hasKey(VOID_EXCESS_NBT_KEY)) {
            // backward compatibility
            mode = nbt.getBoolean(VOID_EXCESS_NBT_KEY) ? VoidingMode.VOID_ALL : VoidingMode.VOID_NONE;
        }
        if (mode != null && getAllowedVoidingModes().contains(mode)) {
            voidingMode = mode;
        }
    }

    @Override
    public void loadComponentData(@NotNull NBTTagCompound nbt) {
        loadLegacyNBTData(nbt);
    }
}
