package gregtech.api.ec.implementations;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.enums.ECHatchElement.*;
import static gregtech.api.enums.GTValues.V;
import static gregtech.api.metatileentity.implementations.MTEMultiBlockBase.INTERRUPT_SOUND_INDEX;
import static gregtech.api.util.GTUtility.*;
import static mcp.mobius.waila.api.SpecialChars.GREEN;
import static mcp.mobius.waila.api.SpecialChars.RED;
import static mcp.mobius.waila.api.SpecialChars.RESET;
import static net.minecraft.util.StatCollector.translateToLocal;
import static net.minecraft.util.StatCollector.translateToLocalFormatted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.annotation.Nonnegative;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.google.common.collect.Lists;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.IStructureWalker;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IActivitySoundLoopComponent;
import gregtech.api.ec.IClientSoundComponent;
import gregtech.api.ec.IComponent;
import gregtech.api.ec.IEnergyOutputLogicComponent;
import gregtech.api.ec.IMachineModeComponent;
import gregtech.api.ec.IOnActiveStateChangeComponent;
import gregtech.api.ec.IOnClientPostTickComponent;
import gregtech.api.ec.IOnFirstTickComponent;
import gregtech.api.ec.IOnMachineRemovalComponent;
import gregtech.api.ec.IOnMachineStopComponent;
import gregtech.api.ec.IOnMachineUnloadComponent;
import gregtech.api.ec.IOnScrewdriverClickComponent;
import gregtech.api.ec.IOnServerPostTickComponent;
import gregtech.api.ec.IOnServerTickFailComponent;
import gregtech.api.ec.IOnSolderingToolClickComponent;
import gregtech.api.ec.IOnWireCutterClickComponent;
import gregtech.api.ec.IOnWorkStateChangeComponent;
import gregtech.api.ec.IOnWrenchClickComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.ISavableComponent;
import gregtech.api.ec.IScannerInfoComponent;
import gregtech.api.ec.IWailaBodyProviderComponent;
import gregtech.api.ec.IWailaDataComponent;
import gregtech.api.ec.multiblock.IBatchConfigComponent;
import gregtech.api.ec.multiblock.IDualInputLogicComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IHatchContainerComponent;
import gregtech.api.ec.multiblock.IInputSeparationComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidOutputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockItemOutputLogicComponent;
import gregtech.api.ec.multiblock.IRecipeLockingComponent;
import gregtech.api.ec.multiblock.IStructureCheckHandlerComponent;
import gregtech.api.ec.multiblock.IStructureLogicComponent;
import gregtech.api.ec.multiblock.IVoidingConfigComponent;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.HarvestTool;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.VoidingMode;
import gregtech.api.interfaces.IOutputBus;
import gregtech.api.interfaces.IOutputHatch;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.metatileentity.IMultiblockController;
import gregtech.api.interfaces.modularui.IControllerWithOptionalFeatures;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.SingleRecipeCheck;
import gregtech.api.structure.StructureChecker;
import gregtech.api.structure.error.ErrorType;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTClientPreference;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GTWaila;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.ParallelHelper;
import gregtech.api.util.VoidProtectionHelper;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.SmartHatchLogicComponent;
import gregtech.common.ec.multiblock.StructureLogicComponent;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockECBaseGui;
import gregtech.common.pollution.Pollution;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IHatchWatcher;
import gregtech.common.tileentities.machines.RecipeCheckReason;
import io.netty.buffer.ByteBuf;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public abstract class MTEMultiBlockECBase<T extends MTEMultiBlockECBase<T>> extends MetaTileEntity
    implements IControllerWithOptionalFeatures, IHatchWatcher, IECMultiblock, IMultiblockController, IAlignment,
    ISurvivalConstructable, ISecondaryDescribable {

    protected static final int SURVIVOR_BUILD_DONE = -1;
    protected static final int SURVIVOR_BUILD_UNSUPPORT = -2;
    static {
        if (GTUtility.isClient()) {
            addTooltipResetListener();
        }
    }

    private Object[] components = GTValues.emptyObjectArray;

    private ExtendedFacing mExtendedFacing = ExtendedFacing.DEFAULT;
    private IAlignmentLimits mLimits = getInitialAlignmentLimits();
    private final StructureCenterWalker centerWalker = new StructureCenterWalker();
    private final Vector3f center = new Vector3f();
    private int structureRadius;
    protected long mTotalRunTime = 0;

    private static final AtomicReferenceArray<MultiblockTooltipBuilder> tooltips = new AtomicReferenceArray<>(
        GregTechAPI.METATILEENTITIES.length);

    public MTEMultiBlockECBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2);
    }

    public MTEMultiBlockECBase(String aName) {
        super(aName, 2);
    }

    private boolean componentsInitialized = false;
    private boolean componentsReady = false;

    protected final void ensureComponentsInitialized() {
        if (!componentsInitialized) initComponents();
    }

    protected void initComponents() {
        if (componentsInitialized) return;
        componentsInitialized = true;
        components = new Object[Math.max(32, ComponentKey.getKeyCount())];
        registerComponents();
        onComponentsReady();
        componentsReady = true;
    }

    private boolean lastActive = false;
    protected IOnServerPostTickComponent[] serverPostTicks = IOnServerPostTickComponent.KEY.emptyArray;
    protected IOnClientPostTickComponent[] clientPostTicks = IOnClientPostTickComponent.KEY.emptyArray;
    protected IOnActiveStateChangeComponent[] activeStateChanges = IOnActiveStateChangeComponent.KEY.emptyArray;
    protected IStructureLogicComponent structureLogic;
    protected @Nullable IMaintenanceLogicComponent maintenanceLogic;

    protected void onComponentsReady() {
        serverPostTicks = getComponents(IOnServerPostTickComponent.KEY);
        clientPostTicks = getComponents(IOnClientPostTickComponent.KEY);
        activeStateChanges = getComponents(IOnActiveStateChangeComponent.KEY);
        structureLogic = getComponent(IStructureLogicComponent.KEY);
        maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.KEY);
        for (var component : getComponents(IComponent.KEY)) {
            component.onComponentsReady();
        }
        if (componentsReady) {
            var igte = getBaseMetaTileEntity();
            if (igte != null && igte.isClientSide()) {
                // update cached mNeedsClientTick
                igte.setMetaTileEntity(this);
                if (clientPostTicks.length == 0) {
                    igte.tryDisableTicking();
                } else {
                    igte.enableTicking();
                }
            }
        }
    }

    @Override
    public final IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        var instance = createNewMetaEntity(aTileEntity);
        instance.initComponents();
        return instance;
    }

    protected abstract @NotNull T createNewMetaEntity(IGregTechTileEntity aTileEntity);

    private class CheckHandler extends BaseMultiblockComponent implements IStructureCheckHandlerComponent {

        @Override
        public void checkMachine(@NotNull IGregTechTileEntity igte, @NotNull List<StructureError> errors) {
            MTEMultiBlockECBase.this.checkMachine(igte, getControllerSlot(), errors);

            StructureSize size = centerWalker.finish();

            if (size != null) {
                center.set(size.centerX, size.centerY, size.centerZ);
                structureRadius = size.radius;
            } else {
                center.set(igte.getXCoord(), igte.getYCoord(), igte.getZCoord());
                structureRadius = 1;
            }
        }

        @Override
        public int getStructureCheckPriority() {
            return StructureCheckPriority.MACHINE_CHECK.priority;
        }
    }

    protected void registerComponents() {
        addComponent(new StructureLogicComponent());
        addComponent(new CheckHandler());
        addComponent(new SmartHatchLogicComponent());
        MultiblockComponentBuilder builder = new MultiblockComponentBuilder();
        configureComponents(builder);
        builder.build(this, this::createProcessingLogic, this::getRecipeMap);
    }

    protected void configureComponents(@NotNull MultiblockComponentBuilder builder) {}

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IComponent> @Nullable C tryGetComponent(@NotNull ComponentKey.UniqueKey<C> key) {
        ensureComponentsInitialized();
        int id = key.id;
        return id < components.length ? (C) components[id] : null;
    }

    @Override
    public <C extends IComponent> boolean hasComponent(@NotNull ComponentKey<C> key) {
        ensureComponentsInitialized();
        int id = key.id;
        return id < components.length && components[id] != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IComponent> @NotNull C[] getComponents(@NotNull ComponentKey.ListKey<C> key) {
        ensureComponentsInitialized();
        int id = key.id;
        if (id < components.length) {
            C[] result = (C[]) components[id];
            if (result != null) return result;
        }
        return key.emptyArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IComponent> T addComponent(@NotNull C component) {
        var keys = ComponentKey.getKeysForImpl(component.getClass());
        int maxId = -1;
        for (var key : keys) {
            if (key.id > maxId) maxId = key.id;
            if (key instanceof ComponentKey.UniqueKey<?> && key.id < components.length && components[key.id] != null) {
                throw new IllegalStateException(
                    String.format(
                        "Can't add multiple unique components for interface %s, attempted to add %s, but already has %s",
                        key.interfaceType.getSimpleName(),
                        component.getClass()
                            .getSimpleName(),
                        components[key.id]
                            .getClass()
                            .getSimpleName()));
            }
        }
        if (maxId >= components.length) {
            components = Arrays.copyOf(components, Math.max(maxId + 1, ComponentKey.getKeyCount()));
        }
        for (var key : keys) {
            switch (key) {
                case ComponentKey.UniqueKey<?> _ -> components[key.id] = component;
                case ComponentKey.ListKey<?> listKey -> components[key.id] = listKey.append(components[key.id], component);
            }
        }
        component.onAddToMachine(this);
        if (componentsReady) {
            onComponentsReady();
        }
        return (T) this;
    }

    private <C extends IComponent> boolean removeComponentInternal(@NotNull C component) {
        boolean removed = false;
        for (var key : ComponentKey.getKeysForImpl(component.getClass())) {
            if (key.id >= components.length) continue;
            int id = key.id;
            var current = components[id];
            if (current == null) return false;
            switch (key) {
                case ComponentKey.UniqueKey<?> _ -> {
                    if (current == component) {
                        components[id] = null;
                        removed = true;
                    }
                }
                case ComponentKey.ListKey<?> listKey -> {
                    Object updated = listKey.remove(current, component);
                    if (updated != current) {
                        components[id] = updated;
                        removed = true;
                    }
                }
            }
        }
        return removed;
    }

    @Override
    public <C extends IComponent> boolean removeComponent(@NotNull C component) {
        boolean removed = removeComponentInternal(component);
        if (removed) {
            component.onRemoveFromMachine(this);
            if (componentsReady) {
                onComponentsReady();
            }
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IComponent> boolean removeComponent(@NotNull ComponentKey.ListKey<C> key) {
        int id = key.id;
        if (id >= components.length || components[id] == null) return false;

        var targets = (C[]) components[id];
        boolean removed = false;
        for (var target : targets) {
            if (removeComponentInternal(target)) {
                target.onRemoveFromMachine(this);
                removed = true;
            }
        }
        if (removed && componentsReady) {
            onComponentsReady();
        }
        return removed;
    }

    @SideOnly(Side.CLIENT)
    private static void addTooltipResetListener() {
        SimpleReloadableResourceManager manager = (SimpleReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager();

        manager.reloadListeners.add(resourceManager -> {
            final int len = tooltips.length();

            for (int i = 0; i < len; i++) {
                tooltips.set(i, null);
            }
        });
    }

    protected abstract MultiblockTooltipBuilder createTooltip();

    protected final MultiblockTooltipBuilder getTooltip() {
        int tId = getBaseMetaTileEntity().getMetaTileID();
        MultiblockTooltipBuilder tooltip = tooltips.get(tId);
        if (tooltip == null) {
            tooltip = createTooltip();
            tooltips.set(tId, tooltip);
        }
        return tooltip;
    }

    @Override
    public final String[] getDescription() {
        return getCurrentDescription();
    }

    @Override
    public String getLocalName() {
        return super.getLocalName();
    }

    @Override
    public final String[] getPrimaryDescription() {
        return getTooltip().getInformation();
    }

    @Override
    public final String[] getSecondaryDescription() {
        return getTooltip().getStructureInformation();
    }

    @Override
    public final boolean isDisplaySecondaryDescription() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ITexture[][] getInventoryTextures() {
        return getOrCreateInventoryTextures();
    }

    @Override
    public boolean allowCoverOnSide(ForgeDirection side, ItemStack coverItem) {
        return side != getBaseMetaTileEntity().getFrontFacing();
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        for (var comp : getComponents(IOnScrewdriverClickComponent.KEY)) {
            if (comp.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, aTool)) return;
        }
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return aIndex > 0;
    }

    @Override
    public int getProgresstime() {
        var comp = tryGetComponent(IRecipeLogicComponent.KEY);
        return comp != null ? comp.getProgressTime() : 0;
    }

    @Override
    public int maxProgresstime() {
        var comp = tryGetComponent(IRecipeLogicComponent.KEY);
        return comp != null ? comp.getMaxProgressTime() : 0;
    }

    @Override
    public int increaseProgress(int aProgress) {
        return aProgress;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setLong("mTotalRunTime", mTotalRunTime);
        aNBT.setBoolean("makePowerfailEvents", makePowerfailEvents);
        aNBT.setByte(
            "eRotation",
            (byte) mExtendedFacing.getRotation()
                .getIndex());
        aNBT.setByte(
            "eFlip",
            (byte) mExtendedFacing.getFlip()
                .getIndex());
        for (var component : getComponents(ISavableComponent.KEY)) {
            component.saveNBTData(aNBT);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        mTotalRunTime = aNBT.getLong("mTotalRunTime");
        makePowerfailEvents = !aNBT.hasKey("makePowerfailEvents") || aNBT.getBoolean("makePowerfailEvents");
        mExtendedFacing = ExtendedFacing.of(
            getBaseMetaTileEntity().getFrontFacing(),
            Rotation.byIndex(aNBT.getByte("eRotation")),
            Flip.byIndex(aNBT.getByte("eFlip")));
        if (!getAlignmentLimits().isNewExtendedFacingValid(mExtendedFacing)) {
            mExtendedFacing = getCorrectedAlignment(mExtendedFacing);
        }
        for (var component : getComponents(ISavableComponent.KEY)) {
            component.loadNBTData(aNBT);
        }
    }

    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.isServerSide() && GTUtil.saveMultiblockInputConfiguration(this, aPlayer)) {
            aPlayer.addChatComponentMessage(new ChatComponentTranslation("GT5U.MULTI_MACHINE_CONFIG.SAVE"));
            return;
        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (GTUtil.hasMultiblockInputConfiguration(aPlayer.getHeldItem())) {
            if (aBaseMetaTileEntity.isServerSide()) {
                if (GTUtil.loadMultiblockInputConfiguration(this, aPlayer)) {
                    aPlayer.addChatComponentMessage(new ChatComponentTranslation("GT5U.MULTI_MACHINE_CONFIG.LOAD"));
                } else {
                    aPlayer
                        .addChatComponentMessage(new ChatComponentTranslation("GT5U.MULTI_MACHINE_CONFIG.LOAD.FAIL"));
                }
            }
            return true;
        }
        openGui(aPlayer);
        return true;
    }

    @Override
    public byte getTileEntityBaseType() {
        return HarvestTool.WrenchLevel2.toTileEntityBaseType();
    }

    @Override
    public void onMachineBlockUpdate() {
        structureLogic.markStructureDirty();
    }

    @Override
    public long getTotalRunTime() {
        return mTotalRunTime;
    }

    public void setTotalRunTime(long mTotalRunTime) {
        this.mTotalRunTime = mTotalRunTime;
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        for (var comp : getComponents(IOnFirstTickComponent.KEY)) {
            comp.onFirstTick(aBaseMetaTileEntity);
        }
    }

    private void updateActiveState(@NotNull IGregTechTileEntity igte, boolean active) {
        if (lastActive == active) return;
        lastActive = active;
        if (active) {
            for (int i = 0; i < activeStateChanges.length; i++) {
                activeStateChanges[i].onMachineActivated(igte);
            }
        } else {
            for (int i = 0; i < activeStateChanges.length; i++) {
                activeStateChanges[i].onMachineDeactivated(igte);
            }
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide()) {
            // Time Counter
            mTotalRunTime++;

            for (int i = 0; i < serverPostTicks.length; i++) {
                serverPostTicks[i].onServerPostTick(aBaseMetaTileEntity, aTick);
            }
        } else {
            if (clientPostTicks.length == 0) {
                aBaseMetaTileEntity.tryDisableTicking();
            } else {
                for (int i = 0; i < clientPostTicks.length; i++) {
                    clientPostTicks[i].onClientPostTick(aBaseMetaTileEntity, aTick);
                }
            }
        }
    }

    @Override
    public boolean isFormed() {
        return structureLogic.isFormed();
    }

    @Override
    public boolean needsClientTick() {
        return componentsInitialized && clientPostTicks.length > 0;
    }

    @Override
    public void onClientSoundStateChanged() {
        var activitySoundLoop = tryGetComponent(IActivitySoundLoopComponent.KEY);
        if (activitySoundLoop != null) activitySoundLoop.startActivitySound();
    }

    @Override
    public void onTickFail(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onTickFail(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isServerSide()) {
            aBaseMetaTileEntity.disableWorking();
            for (var component : getComponents(IOnServerTickFailComponent.KEY)) {
                component.onServerTickFail(aBaseMetaTileEntity, aTick);
            }
        }
    }

    @Override
    public void scheduleRecipeCheck(RecipeCheckReason reason) {
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) recipeLogic.scheduleRecipeCheck(reason);
    }

    @Override
    public void onDisableWorking() {
        for (var component : getComponents(IOnWorkStateChangeComponent.KEY)) {
            component.onDisableWorking();
        }
    }

    @Override
    public void onEnableWorking() {
        for (var component : getComponents(IOnWorkStateChangeComponent.KEY)) {
            component.onEnableWorking();
        }
    }

    @Override
    public void doSound(byte aIndex, double aX, double aY, double aZ) {
        super.doSound(aIndex, aX, aY, aZ);
        for (var component : getComponents(IClientSoundComponent.KEY)) {
            if (component.onDoSound(aIndex, aX, aY, aZ)) return;
        }
        if (aIndex == INTERRUPT_SOUND_INDEX) {
            GTUtility.doSoundAtClient(SoundResource.IC2_MACHINES_INTERRUPT_ONE, 100, 1.0F, aX, aY, aZ);
        }
    }

    @Override
    public void startSoundLoop(byte aIndex, double aX, double aY, double aZ) {
        super.startSoundLoop(aIndex, aX, aY, aZ);
        for (var component : getComponents(IClientSoundComponent.KEY)) {
            component.onStartSoundLoop(aIndex, aX, aY, aZ);
        }
    }

    /**
     * Checks the Machine. You have to assign the MetaTileEntities for the Hatches here.
     * Any error must be added to the error list.
     */
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
        if (errors.isEmpty()) {
            // Only add this if we do not emit any diagnostics.
            errors.add(StructureErrorRegistry.UNKNOWN_STRUCTURE_ERROR);
        }
    }

    /**
     * @deprecated Use {@link #stopMachine(ShutDownReason)}
     */
    @Deprecated
    public void stopMachine() {
        stopMachine(ShutDownReasonRegistry.NONE);
    }

    /**
     * @deprecated Use {@link #stopMachine(ShutDownReason)}
     */
    @Deprecated
    public void criticalStopMachine() {
        stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
    }

    @Override
    public void onSetActive(boolean active) {
        super.onSetActive(active);
        var igte = getBaseMetaTileEntity();
        if (igte != null && igte.isServerSide()) {
            updateActiveState(igte, active);
        }
    }

    public void stopMachine(@NotNull ShutDownReason reason) {
        if (!ShutDownReasonRegistry.isRegistered(reason.getID())) {
            throw new RuntimeException(String.format("Reason %s is not registered for registry", reason.getID()));
        }
        for (var component : getComponents(IOnMachineStopComponent.KEY)) {
            component.onMachineStop(reason);
        }
        IGregTechTileEntity igte = getBaseMetaTileEntity();
        if (igte != null) {
            igte.setShutDownReason(reason);
            igte.setShutdownStatus(true);
            igte.disableWorking();
            igte.setActive(false);
        }
        if (reason.wasCritical()) {
            sendSound(INTERRUPT_SOUND_INDEX);
        }
        if (makePowerfailEvents && reason == ShutDownReasonRegistry.POWER_LOSS) {
            GTMod.proxy.powerfailTracker.createPowerfailEvent(igte);
        }
    }

    @Override
    public ModularWindow createPowerPanel(EntityPlayer player) {
        throw new UnsupportedOperationException("EC machines only supports MUI2");
    }

    public int getRepairStatus() {
        var maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.KEY);
        return maintenanceLogic != null ? maintenanceLogic.getRepairStatus() : 0;
    }

    public int getIdealStatus() {
        var maintenanceLogic = tryGetComponent(IMaintenanceLogicComponent.KEY);
        return maintenanceLogic != null ? maintenanceLogic.getIdealStatus() : 0;
    }

    public void explodeMultiblock() {
        GTLog.writeExplosionLog(this, "MultiBlockExplosion");

        Pollution.addPollution(getBaseMetaTileEntity(), GTMod.proxy.mPollutionOnExplosion);
        mInventory[1] = null;
        for (var component : getComponents(IHatchContainerComponent.KEY)) {
            component.explodeAllHatches(V[8]);
        }
        getBaseMetaTileEntity().doExplosion(V[8]);
    }

    public boolean addEnergyOutput(long aEU) {
        return getComponent(IEnergyOutputLogicComponent.KEY).addEnergyOutput(aEU);
    }

    public boolean addEnergyOutputMultipleDynamos(long aEU, boolean aAllowMixedVoltageDynamos) {
        return getComponent(IEnergyOutputLogicComponent.KEY)
            .addEnergyOutputMultipleDynamos(aEU, aAllowMixedVoltageDynamos);
    }

    /**
     * Drains fluid from the given hatch, including {@link IDualInputHatch}.
     * Note that you should not modify any hatch content during recipe check as it will
     * lead to possible failed extraction when the recipe processing tries to extract
     * the recipe from the hatch. Doing simulate check is fine, however.
     *
     * @param doDrain If false, fluid will not actually be consumed
     * @return Whether the hatch contains enough fluid to drain
     */
    public boolean drain(MTEHatch hatch, FluidStack fluid, boolean doDrain) {
        if (fluid == null || hatch == null) return false;
        if (hasComponent(IDualInputLogicComponent.KEY) && hatch instanceof IDualInputHatch tHatch
            && tHatch.supportsFluids()) {
            Optional<IDualInputInventory> inventory = tHatch.getFirstNonEmptyInventory();
            if (inventory.isPresent()) {
                for (FluidStack storedFluid : Lists.newArrayList(
                    inventory.get()
                        .getFluidInputs())) {
                    if (fluid.isFluidEqual(storedFluid)) {
                        if (doDrain) storedFluid.amount = Math.max(storedFluid.amount - fluid.amount, 0);
                        return storedFluid.amount >= fluid.amount;
                    }
                }
            }
        }

        if (hatch instanceof MTEHatchInput tHatch && tHatch.isValid()) {
            FluidStack tFluid = tHatch.drain(ForgeDirection.UNKNOWN, fluid, doDrain);
            return tFluid != null && tFluid.amount >= fluid.amount;
        }

        return false;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        var recipeLogic = tryGetComponent(IRecipeLogicComponent.KEY);
        if (recipeLogic != null) {
            return recipeLogic.getRecipeMap();
        }
        return null;
    }

    /**
     * Creates logic to run recipe check based on recipemap. This runs only once, on class instantiation.
     * <p>
     * If this machine doesn't use recipemap or does some complex things, override @link #checkProcessing().
     */
    @ApiStatus.OverrideOnly
    protected @NotNull ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic();
    };

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        List<String> infoData = new ArrayList<>();

        if (getBaseMetaTileEntity() != null) {
            IGregTechTileEntity te = getBaseMetaTileEntity();

            infoData
                .add(StatCollector.translateToLocalFormatted("GT5U.multiblock.scanner.owned_by", te.getOwnerName()));

            if (te.getMetaTileEntity() != null) {
                infoData.add(
                    StatCollector
                        .translateToLocalFormatted("GT5U.multiblock.scanner.meta_tile_entity", te.getMetaTileID()) + " "
                        + StatCollector.translateToLocal(
                            te.canAccessData() ? "GT5U.multiblock.scanner.valid" : "GT5U.multiblock.scanner.invalid"));
            } else {
                infoData.add(StatCollector.translateToLocal("GT5U.multiblock.scanner.is_meta_tile_entity"));
            }
        }

        for (var component : getComponents(IScannerInfoComponent.KEY)) {
            component.appendInfoData(infoData);
        }

        long seconds = (this.mTotalRunTime / 20);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        // spotless:off
        String timeValue =
            hours > 0 ? String.valueOf(hours)
                : minutes > 0 ? String.valueOf(minutes)
                  : seconds > 0 ? String.valueOf(seconds)
                    : String.valueOf(this.mTotalRunTime);
        String timeKey =
            hours > 0 ? "GT5U.multiblock.scanner.totalRunHours"
                : minutes > 0 ? "GT5U.multiblock.scanner.totalRunMinutes"
                  : seconds > 0 ? "GT5U.multiblock.scanner.totalRunSeconds"
                    : "GT5U.multiblock.scanner.totalRunTicks";
        // spotless:on
        infoData.add(StatCollector.translateToLocalFormatted(timeKey, timeValue));

        return infoData.toArray(new String[0]);
    }

    @Override
    public Map<String, String> getInfoMap() {
        Map<String, String> infoMap = new HashMap<>();
        for (var component : getComponents(IScannerInfoComponent.KEY)) {
            component.appendInfoMap(infoMap);
        }
        return infoMap;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return supportsSlotAutomation(aIndex);
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return supportsSlotAutomation(aIndex);
    }

    protected boolean showProgressInWaila() {
        return hasComponent(IRecipeLogicComponent.KEY);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();

        if (tag.getBoolean("incompleteStructure")) {
            currentTip.add(RED + translateToLocalFormatted("GT5U.waila.multiblock.status.incomplete") + RESET);
            super.getWailaBody(itemStack, currentTip, accessor, config);
            return;
        }

        String efficiency = tag.hasKey("efficiency")
            ? RESET + translateToLocalFormatted("GT5U.waila.multiblock.status.efficiency", tag.getFloat("efficiency"))
            : "";
        if (tag.getBoolean("hasProblems")) {
            currentTip.add(RED + translateToLocal("GT5U.waila.multiblock.status.has_problem") + efficiency);
        } else if (!tag.getBoolean("incompleteStructure")) {
            currentTip.add(GREEN + translateToLocal("GT5U.waila.multiblock.status.running_fine") + efficiency);
        }

        boolean isActive = tag.getBoolean("isActive");

        if (isActive) {
            long energyTier = tag.getLong("energyTier");
            long actualEnergyUsage = tag.getLong("energyUsage");
            if (energyTier > 0) {
                if (actualEnergyUsage > 0) {
                    currentTip.add(
                        translateToLocalFormatted(
                            "GT5U.waila.energy.use_with_amperage",
                            formatNumber(actualEnergyUsage),
                            GTUtility.getAmperageForTier(actualEnergyUsage, (byte) energyTier),
                            GTUtility.getColoredTierNameFromTier((byte) energyTier)));
                } else if (actualEnergyUsage < 0) {
                    currentTip.add(
                        translateToLocalFormatted(
                            "GT5U.waila.energy.produce_with_amperage",
                            formatNumber(-actualEnergyUsage),
                            GTUtility.getAmperageForTier(-actualEnergyUsage, (byte) energyTier),
                            GTUtility.getColoredTierNameFromTier((byte) energyTier)));
                }
            } else {
                if (actualEnergyUsage > 0) {
                    currentTip.add(
                        translateToLocalFormatted(
                            "GT5U.waila.energy.use",
                            formatNumber(actualEnergyUsage),
                            GTUtility.getColoredTierNameFromVoltage(actualEnergyUsage)));
                } else if (actualEnergyUsage < 0) {
                    currentTip.add(
                        translateToLocalFormatted(
                            "GT5U.waila.energy.produce",
                            formatNumber(-actualEnergyUsage),
                            GTUtility.getColoredTierNameFromVoltage(-actualEnergyUsage)));
                }
            }
        }

        boolean isLockedToRecipe = tag.getBoolean("isLockedToRecipe");
        String lockedRecipe = tag.getString("lockedRecipeName");

        if (!isActive && isLockedToRecipe && !lockedRecipe.isEmpty()) {
            // Display locked recipe when the machine is idle
            currentTip.add(translateToLocal("GT5U.waila.multiblock.status.locked_recipe"));
            String[] lines = lockedRecipe.split("\n");
            currentTip.addAll(Arrays.asList(lines));
        } else if (isActive) {
            int outputItemLength = tag.getInteger("outputItemLength");
            int outputFluidLength = tag.getInteger("outputFluidLength");
            int totalOutputs = outputItemLength + outputFluidLength;

            if (totalOutputs > 0) {
                currentTip.add(translateToLocal("GT5U.waila.producing"));
                if (isLockedToRecipe) {
                    currentTip.add(translateToLocal("GT5U.waila.multiblock.status.locked_recipe"));
                }
                for (int i = 0; i < min(3, outputItemLength); i++) {
                    // Localize on the client: the NBT holds the raw stack, not the display name
                    ItemStack outputStack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("outputItemStack" + i));
                    String itemName = outputStack != null ? outputStack.getDisplayName() : "";
                    currentTip.add(
                        "  " + tag.getString("outputItemIcon" + i)
                            + EnumChatFormatting.AQUA
                            + itemName
                            + EnumChatFormatting.RESET
                            + " x "
                            + EnumChatFormatting.GOLD
                            + formatNumber(tag.getInteger("outputItemCount" + i)));
                }
                for (int i = 0; i < min(3 - outputItemLength, outputFluidLength); i++) {
                    // Localize on the client: the NBT holds the internal fluid name, not the display name
                    String internalName = tag.getString("outputFluidName" + i);
                    Fluid fluid = FluidRegistry.getFluid(internalName);
                    String fluidName = fluid != null ? new FluidStack(fluid, 1).getLocalizedName() : internalName;
                    currentTip.add(
                        "  " + tag.getString("outputFluidIcon" + i)
                            + EnumChatFormatting.AQUA
                            + fluidName
                            + EnumChatFormatting.RESET
                            + " x "
                            + EnumChatFormatting.GOLD
                            + formatNumber(tag.getInteger("outputFluidCount" + i))
                            + "L");
                }
                if (totalOutputs > 3) {
                    currentTip.add(
                        translateToLocalFormatted("GT5U.waila.producing.andmore", formatNumber((totalOutputs - 3))));
                }
            }
        }
        // disables progress time display if recipe text is hidden
        if (showProgressInWaila()) {
            currentTip.add(
                GTWaila.getMachineProgressString(
                    isActive,
                    tag.getBoolean("isAllowedToWork"),
                    tag.getInteger("maxProgress"),
                    tag.getInteger("progress")));
        }
        // Show ns on the tooltip
        if (GTMod.proxy.wailaAverageNS && tag.hasKey("averageNS")) {
            int tAverageTime = tag.getInteger("averageNS");
            currentTip
                .add(translateToLocalFormatted("GT5U.waila.multiblock.status.cpu_load", formatNumber(tAverageTime)));
        }

        final int localMaxParallels = tag.getInteger("maxParallelRecipes");
        if (localMaxParallels > 1) {
            String tooltip;
            if (tag.hasKey("powerPanelMaxParallel")) {
                tooltip = translateToLocalFormatted(
                    "GT5U.multiblock.parallelism_override",
                    tag.getInteger("powerPanelMaxParallel"),
                    localMaxParallels);
            } else {
                tooltip = translateToLocal("GT5U.multiblock.parallelism") + ": "
                    + EnumChatFormatting.WHITE
                    + localMaxParallels;
            }

            currentTip.add(tooltip);
        }

        if (tag.hasKey("mode")) {
            currentTip.add(
                StatCollector.translateToLocal("GT5U.multiblock.runningMode") + " "
                    + EnumChatFormatting.WHITE
                    + tag.getString("mode")
                    + EnumChatFormatting.RESET);
        }

        for (var component : getComponents(IWailaBodyProviderComponent.KEY)) {
            component.appendWailaBody(itemStack, currentTip, accessor, config);
        }

        getExtraWailaBody(itemStack, currentTip, tag, accessor, config);

        super.getWailaBody(itemStack, currentTip, accessor, config);
    }

    public final void getMTEWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);

        tag.setBoolean("incompleteStructure", !isFormed());

        final IGregTechTileEntity tileEntity = getBaseMetaTileEntity();
        if (tileEntity != null) {
            tag.setBoolean("isActive", tileEntity.isActive());
            tag.setBoolean("isAllowedToWork", tileEntity.isAllowedToWork());
        }

        final GTClientPreference preference = GTMod.proxy.getClientPreference(player.getUniqueID());
        if (preference != null && preference.isWailaAverageNSEnabled()) {
            getBaseMetaTileEntity().startTimeStatistics();
            int tAverageTime = 0;
            int amountOfZero = 0;
            for (int tTime : this.getBaseMetaTileEntity()
                .getTimeStatistics()) {
                tAverageTime += tTime;
                if (tTime == 0) {
                    amountOfZero += 1;
                }
            }

            // tick time zero means it has not been updated yet
            int samples = getBaseMetaTileEntity().getTimeStatistics().length - amountOfZero;
            if (samples > 0) {
                tag.setInteger("averageNS", tAverageTime / samples);
            }
        }

        for (var component : getComponents(IWailaDataComponent.KEY)) {
            component.appendWailaNBTData(player, tile, tag, world, x, y, z);
        }

        getExtraWailaNBT(player, tile, tag, world, x, y, z);
    }

    public void getExtraWailaNBT(EntityPlayerMP playerMP, TileEntity tileEntity, NBTTagCompound tag, World world, int x,
        int y, int z) {}

    public void getExtraWailaBody(ItemStack itemStack, List<String> list, NBTTagCompound tag,
        IWailaDataAccessor accessor, IWailaConfigHandler config) {}

    @Override
    public final boolean isDeprecated() {
        return getTooltip().isDeprecated();
    }

    @Override
    protected void onGuiOpened(EntityPlayer player) {
        super.onGuiOpened(player);

        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte != null && igte.isServerSide()) {
            GTMod.proxy.powerfailTracker.removePowerfailEvents(igte);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        IGregTechTileEntity igte = getBaseMetaTileEntity();
        if (igte != null && igte.isServerSide()) {
            GTMod.proxy.powerfailTracker.removePowerfailEvents(igte);
            updateActiveState(igte, false);
        }
        for (var component : getComponents(IOnMachineRemovalComponent.KEY)) {
            component.onMachineRemoval();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        IGregTechTileEntity igte = getBaseMetaTileEntity();
        if (igte != null && igte.isServerSide()) {
            updateActiveState(igte, false);
        }
        for (var component : getComponents(IOnMachineUnloadComponent.KEY)) {
            component.onMachineUnload();
        }
    }

    /**
     * Check if there is 1 TT Energy Hatch OR up to 2 Energy Hatches
     */
    public void checkExoticAndNormalEnergyHatches(List<StructureError> errors) {
        var comp = tryGetComponent(IMultiblockEnergyInputLogicComponent.KEY);
        var energyHatches = comp != null ? comp.getNormalEnergyHatches() : Collections.emptyList();
        var exoticHatches = comp != null ? comp.getExoticEnergyHatches() : Collections.emptyList();
        if (exoticHatches.isEmpty() && energyHatches.isEmpty()) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, Energy, 0, 1));
        }

        if (!exoticHatches.isEmpty()) {
            if (!energyHatches.isEmpty()) {
                errors.add(StructureErrors.of("GT5U.gui.text.structure_error.one_tt_or_two_energy"));
                return;
            }

            if (exoticHatches.size() != 1) {
                errors.add(StructureErrors.of("GT5U.gui.text.structure_error.one_tt_or_two_energy"));
                return;
            }
        }
        if (energyHatches.size() > 2) {
            errors.add(StructureErrors.of("GT5U.gui.text.structure_error.one_tt_or_two_energy"));
        }
    }

    /**
     * Checks if all the item / fluid outputs of the recipe can be outputted to the buses / hatches. If void protection
     * is enabled, it also checks for {@link #protectsExcessItem()} and {@link #protectsExcessFluid()}, so you don't
     * need to call them along with this method.
     * <p>
     * If you're using {@link ParallelHelper}, it will handle void protection and return 0 parallel if all the output
     * cannot be dumped into buses / hatches. In that case you won't use this method.
     */
    protected boolean canOutputAll(@NotNull GTRecipe recipe) {
        return canOutputAll(recipe.mOutputs, recipe.mFluidOutputs);
    }

    /**
     * Checks if all the items can be outputted to the output buses. If void protection is enabled, it also checks for
     * {@link #protectsExcessItem()}, so you don't need to call it along with this method.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean canOutputAll(ItemStack[] items) {
        return canOutputAll(items, null);
    }

    /**
     * Checks if all the fluids can be outputted to the output hatches. If void protection is enabled, it also checks
     * for {@link #protectsExcessFluid()}, so you don't need to call it along with this method.
     */
    protected boolean canOutputAll(FluidStack[] fluids) {
        return canOutputAll(null, fluids);
    }

    /**
     * Checks if all the items / fluids can be outputted to output buses / hatches. If void protection is enabled, it
     * also checks for {@link #protectsExcessItem()} and {@link #protectsExcessFluid()}, so you don't need to call them
     * along with this method.
     */
    protected boolean canOutputAll(@Nullable ItemStack[] items, @Nullable FluidStack[] fluids) {
        if (!protectsExcessItem() && !protectsExcessFluid()) {
            return true;
        }

        VoidProtectionHelper voidProtectionHelper = new VoidProtectionHelper().setMachine(this)
            .setItemOutputs(items)
            .setFluidOutputs(fluids)
            .build();
        return voidProtectionHelper.getMaxParallel() > 0;
    }

    @Override
    public boolean isAllowedToWork() {
        final IGregTechTileEntity baseMetaTileEntity = getBaseMetaTileEntity();
        return baseMetaTileEntity != null && baseMetaTileEntity.isAllowedToWork();
    }

    @Override
    public void disableWorking() {
        final IGregTechTileEntity baseMetaTileEntity = getBaseMetaTileEntity();
        if (baseMetaTileEntity != null) {
            baseMetaTileEntity.disableWorking();
        }
    }

    @Override
    public void enableWorking() {
        final IGregTechTileEntity baseMetaTileEntity = getBaseMetaTileEntity();
        if (baseMetaTileEntity != null) {
            baseMetaTileEntity.enableWorking();
        }
    }

    @Override
    public boolean isMuffled() {
        final IGregTechTileEntity baseMetaTileEntity = getBaseMetaTileEntity();
        if (baseMetaTileEntity != null) {
            return baseMetaTileEntity.isMuffled();
        }
        return false;
    }

    @Override
    public void setMuffled(boolean value) {
        final IGregTechTileEntity baseMetaTileEntity = getBaseMetaTileEntity();
        if (baseMetaTileEntity != null) {
            baseMetaTileEntity.setMuffler(value);
        }
    }

    public ItemStack getControllerSlot() {
        return mInventory[getControllerSlotIndex()];
    }

    public final int getControllerSlotIndex() {
        return 1;
    }

    // True if the slot with index aSlot may be interacted with through automation
    protected boolean supportsSlotAutomation(int aSlot) {
        return false;
    }

    @Override
    public Pos2d getPowerSwitchButtonPos() {
        return new Pos2d(174, 148);
    }

    @Override
    public Pos2d getStructureUpdateButtonPos() {
        return new Pos2d(174, 130);
    }

    @Override
    public int getStructureUpdateTime() {
        return structureLogic.getStructureUpdateTime();
    }

    @Override
    public void setStructureUpdateTime(int time) {
        structureLogic.setStructureUpdateTime(time);
    }

    @Override
    public Pos2d getVoidingModeButtonPos() {
        return new Pos2d(8, 91);
    }

    @Override
    public boolean supportsInputSeparation() {
        return hasComponent(IInputSeparationComponent.KEY);
    }

    @Override
    public boolean isInputSeparationEnabled() {
        var comp = tryGetComponent(IInputSeparationComponent.KEY);
        return comp != null && comp.isInputSeparationEnabled();
    }

    @Override
    public void setInputSeparation(boolean enabled) {
        var comp = tryGetComponent(IInputSeparationComponent.KEY);
        if (comp != null) comp.setInputSeparation(enabled);
    }

    @Override
    public Pos2d getInputSeparationButtonPos() {
        return new Pos2d(26, 91);
    }

    @Override
    public boolean supportsMachineModeSwitch() {
        return hasComponent(IMachineModeComponent.KEY);
    }

    @Override
    public int getMachineMode() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        return comp != null ? comp.getMachineMode() : 0;
    }

    @Override
    public String getMachineModeKey() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        return comp != null ? comp.getMachineModeKey() : IControllerWithOptionalFeatures.super.getMachineModeKey();
    }

    @Override
    public String getMachineModeName() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        return comp != null ? comp.getMachineModeName() : IControllerWithOptionalFeatures.super.getMachineModeName();
    }

    @Override
    public void onMachineModeSwitchClick() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        if (comp != null) comp.onMachineModeSwitchClick();
    }

    @Override
    public @Nullable UITexture getMachineModeIcon(int index) {
        return null;
    }

    @Override
    public void setMachineMode(int index) {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        if (comp != null) comp.setMachineMode(index);
    }

    @Override
    public int nextMachineMode() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        return comp != null ? comp.nextMachineMode() : 0;
    }

    @Override
    public Pos2d getMachineModeSwitchButtonPos() {
        var comp = tryGetComponent(IMachineModeComponent.KEY);
        return comp != null ? comp.getMachineModeSwitchButtonPos() : new Pos2d(80, 91);
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        for (var comp : getComponents(IOnWireCutterClickComponent.KEY)) {
            if (comp.onWireCutterRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool)) return true;
        }
        return super.onWireCutterRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public boolean supportsBatchMode() {
        return hasComponent(IBatchConfigComponent.KEY);
    }

    @Override
    public boolean isBatchModeEnabled() {
        var comp = tryGetComponent(IBatchConfigComponent.KEY);
        return comp != null && comp.isBatchModeEnabled();
    }

    @Override
    public void setBatchMode(boolean enabled) {
        var comp = tryGetComponent(IBatchConfigComponent.KEY);
        if (comp != null) comp.setBatchMode(enabled);
    }

    @Override
    public Pos2d getBatchModeButtonPos() {
        return new Pos2d(44, 91);
    }

    @Override
    public boolean isRecipeLockingEnabled() {
        var recipeLocking = tryGetComponent(IRecipeLockingComponent.KEY);
        return recipeLocking != null && recipeLocking.isRecipeLockingEnabled();
    }

    @Override
    public void setRecipeLocking(boolean enabled) {
        var recipeLocking = tryGetComponent(IRecipeLockingComponent.KEY);
        if (recipeLocking != null) recipeLocking.setRecipeLocking(enabled);
    }

    @Override
    public void setSingleRecipeCheck(SingleRecipeCheck recipeCheck) {
        var recipeLocking = tryGetComponent(IRecipeLockingComponent.KEY);
        if (recipeLocking != null) recipeLocking.setSingleRecipeCheck(recipeCheck);
    }

    @Override
    public SingleRecipeCheck getSingleRecipeCheck() {
        var recipeLocking = tryGetComponent(IRecipeLockingComponent.KEY);
        return recipeLocking != null ? recipeLocking.getSingleRecipeCheck() : null;
    }

    @Override
    public Pos2d getRecipeLockingButtonPos() {
        return new Pos2d(62, 91);
    }

    @Override
    public int getGUIWidth() {
        return 198;
    }

    @Override
    public int getGUIHeight() {
        return 192;
    }

    // Until other features are implemented, this will be the same as supporting parallel.
    @Override
    public boolean supportsPowerPanel() {
        return true;
    }

    @Override
    public Pos2d getPowerPanelButtonPos() {
        return new Pos2d(174, 91);
    }

    @Override
    protected boolean useMui2() {
        return true;
    }

    public boolean supportsInventoryRow() {
        return this.doesBindPlayerInventory();
    }

    public boolean shouldDisplayShutDownReason() {
        return true;
    }

    @Override
    public int getStartUpCheckTimer() {
        return structureLogic.getStartUpCheckTimer();
    }

    @Override
    public final ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        return getGui().build(guiData, syncManager, uiSettings);
    }

    protected @NotNull MTEMultiBlockECBaseGui<?> getGui() {
        return new MTEMultiBlockECBaseGui<>((T) this);
    }

    // todo remove it
    public boolean usesTurbines() {
        return false;
    }

    public boolean canBeMuffled() {
        return true;
    }

    private boolean makePowerfailEvents = true;

    public boolean makesPowerfailEvents() {
        return makePowerfailEvents;
    }

    public void setPowerfailEventCreationStatus(boolean status) {
        makePowerfailEvents = status;
    }

    @Override
    public void setControllerSlot(@Nullable ItemStack item) {
        mInventory[getControllerSlotIndex()] = item;
    }

    @Override
    public int getRandomNumber(int aRange) {
        return getBaseMetaTileEntity().getRandomNumber(aRange);
    }

    @Nullable
    public Vector3f getCenter() {
        return center;
    }

    @Nonnegative
    public int getApproximateRadius() {
        return structureRadius;
    }

    @Override
    public void setExtendedFacing(ExtendedFacing newExtendedFacing) {
        if (mExtendedFacing != newExtendedFacing) {
            mExtendedFacing = newExtendedFacing;
            final IGregTechTileEntity base = getBaseMetaTileEntity();
            structureLogic.markStructureDirty();
            if (base.isServerSide() && !GregTechAPI.isDummyWorld(base.getWorld())) {
                StructureLibAPI.sendAlignment(
                    (IAlignmentProvider) base,
                    new NetworkRegistry.TargetPoint(
                        base.getWorld().provider.dimensionId,
                        base.getXCoord(),
                        base.getYCoord(),
                        base.getZCoord(),
                        512));
            } else {
                base.issueTextureUpdate();
            }
        }
    }

    @Override
    public final boolean isFacingValid(ForgeDirection facing) {
        return canSetToDirectionAny(facing);
    }

    @Override
    public boolean onWrenchRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer entityPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        for (var comp : getComponents(IOnWrenchClickComponent.KEY)) {
            if (comp.onWrenchRightClick(side, wrenchingSide, entityPlayer, aX, aY, aZ, aTool)) return true;
        }
        if (wrenchingSide != getBaseMetaTileEntity().getFrontFacing())
            return super.onWrenchRightClick(side, wrenchingSide, entityPlayer, aX, aY, aZ, aTool);
        if (entityPlayer.isSneaking()) {
            if (isFlipChangeAllowed()) {
                toolSetFlip(getFlip().isHorizontallyFlipped() ? Flip.NONE : Flip.HORIZONTAL);
            } else {
                return false;
            }
        } else {
            if (isRotationChangeAllowed()) {
                toolSetRotation(null);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onSolderingToolRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        for (var comp : getComponents(IOnSolderingToolClickComponent.KEY)) {
            if (comp.onSolderingToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool)) return true;
        }
        return super.onSolderingToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public boolean isActive() {
        var igte = getBaseMetaTileEntity();
        if (igte == null) return false;
        return igte.isActive();
    }

    @Override
    public Set<VoidingMode> getAllowedVoidingModes() {
        var voidingConfig = tryGetComponent(IVoidingConfigComponent.KEY);
        return voidingConfig != null ? voidingConfig.getAllowedVoidingModes()
            : IECMultiblock.super.getAllowedVoidingModes();
    }

    @Override
    public VoidingMode getVoidingMode() {
        var voidingConfig = tryGetComponent(IVoidingConfigComponent.KEY);
        return voidingConfig != null ? voidingConfig.getVoidingMode() : getDefaultVoidingMode();
    }

    @Override
    public void setVoidingMode(VoidingMode mode) {
        var voidingConfig = tryGetComponent(IVoidingConfigComponent.KEY);
        if (voidingConfig != null) voidingConfig.setVoidingMode(mode);
    }

    @Override
    public List<IOutputBus> getOutputBusses() {
        var itemOutputLogic = tryGetComponent(IMultiblockItemOutputLogicComponent.KEY);
        return itemOutputLogic != null ? itemOutputLogic.getOutputBusses() : Collections.emptyList();
    }

    @Override
    public List<IOutputHatch> getOutputHatches(@NotNull List<FluidStack> toOutput) {
        var fluidOutputLogic = tryGetComponent(IMultiblockFluidOutputLogicComponent.KEY);
        return fluidOutputLogic != null ? fluidOutputLogic.getOutputHatches(toOutput) : Collections.emptyList();
    }

    @Override
    public int getItemOutputLimit() {
        var itemOutputLogic = tryGetComponent(IMultiblockItemOutputLogicComponent.KEY);
        return itemOutputLogic != null ? itemOutputLogic.getItemOutputLimit()
            : IECMultiblock.super.getItemOutputLimit();
    }

    @Override
    public int getFluidOutputLimit() {
        var fluidOutputLogic = tryGetComponent(IMultiblockFluidOutputLogicComponent.KEY);
        return fluidOutputLogic != null ? fluidOutputLogic.getFluidOutputLimit()
            : IECMultiblock.super.getFluidOutputLimit();
    }

    @Override
    public boolean canDumpItemToME(List<GTUtility.ItemId> outputs) {
        var itemOutputLogic = tryGetComponent(IMultiblockItemOutputLogicComponent.KEY);
        return itemOutputLogic != null && itemOutputLogic.canDumpItemToME(outputs);
    }

    @Override
    public boolean canDumpFluidToME(List<GTUtility.FluidId> outputs) {
        var fluidOutputLogic = tryGetComponent(IMultiblockFluidOutputLogicComponent.KEY);
        return fluidOutputLogic != null && fluidOutputLogic.canDumpFluidToME(outputs);
    }

    @Override
    protected boolean forceUseMui2() {
        return true;
    }

    @Override
    public void onFacingChange() {
        toolSetDirection(getBaseMetaTileEntity().getFrontFacing());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return mLimits;
    }

    protected void setAlignmentLimits(IAlignmentLimits mLimits) {
        this.mLimits = mLimits;
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return mExtendedFacing;
    }

    /**
     * Due to limitation of Java type system, you might need to do an unchecked cast. HOWEVER, the returned
     * IStructureDefinition is expected to be evaluated against current instance only, and should not be used against
     * other instances, even for those of the same class.
     */
    @Override
    public abstract IStructureDefinition<T> getStructureDefinition();

    @Override
    public abstract void construct(ItemStack stackSize, boolean hintsOnly);

    @Override
    public abstract int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env);

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return getTooltip().getStructureHint();
    }

    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> !f.isVerticallyFliped();
    }

    /**
     * When an old {@link ExtendedFacing} is loaded upon MTE deserialization, and the loaded facing cannot pass test of
     * current {@link #getAlignmentLimits()}, this method will be called to retrieve a corrected version. This method is
     * currently only intended to be used as a mean to migrate alignment limits, so if you never change the alignment
     * limit then you can probably just use the default implementation.
     * <p>
     * The returned new facing must be able to pass the test of {@link #isNewExtendedFacingValid(ExtendedFacing)}
     */
    protected ExtendedFacing getCorrectedAlignment(ExtendedFacing aOldFacing) {
        if (isNewExtendedFacingValid(ExtendedFacing.DEFAULT)) {
            return ExtendedFacing.DEFAULT;
        }
        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            if (facing.getFlip()
                .isVerticallyFliped()) {
                continue;
            }
            if (isNewExtendedFacingValid(facing)) {
                return facing;
            }
        }
        throw new AssertionError("No ExtendedFacing can pass the test of isNewExtendedFacingValid");
    }

    /**
     * Explanation of the world coordinate these offset means:
     * <p>
     * Imagine you stand in front of the controller, with controller facing towards you not rotated or flipped.
     * <p>
     * The horizontalOffset would be the number of blocks on the left side of the controller, not counting controller
     * itself. The verticalOffset would be the number of blocks on the top side of the controller, not counting
     * controller itself. The depthOffset would be the number of blocks between you and controller, not counting
     * controller itself.
     * <p>
     * All these offsets can be negative.
     */
    public final boolean checkPiece(String piece, int horizontalOffset, int verticalOffset, int depthOffset,
        @Nullable List<StructureError> errors) {
        final IGregTechTileEntity tTile = getBaseMetaTileEntity();
        StructureChecker<T> checker = new StructureChecker<>((T) this, !isFormed(), errors);
        getStructureDefinition().iterate(
            piece,
            tTile.getWorld(),
            getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            checker);

        if (checker.success) {
            getStructureDefinition().iterate(
                piece,
                tTile.getWorld(),
                getExtendedFacing(),
                tTile.getXCoord(),
                tTile.getYCoord(),
                tTile.getZCoord(),
                horizontalOffset,
                verticalOffset,
                depthOffset,
                centerWalker);
        }

        return checker.success;
    }

    protected final boolean buildPiece(String piece, ItemStack trigger, boolean hintOnly, int horizontalOffset,
        int verticalOffset, int depthOffset) {
        final IGregTechTileEntity tTile = getBaseMetaTileEntity();
        return getStructureDefinition().buildOrHints(
            (T) this,
            trigger,
            piece,
            tTile.getWorld(),
            getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            hintOnly);
    }

    protected final int survivalBuildPiece(String piece, ItemStack trigger, int horizontalOffset, int verticalOffset,
        int depthOffset, int elementsBudget, ISurvivalBuildEnvironment env, boolean check) {
        final IGregTechTileEntity tTile = getBaseMetaTileEntity();
        return getStructureDefinition().survivalBuild(
            (T) this,
            trigger,
            piece,
            tTile.getWorld(),
            getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            elementsBudget,
            env,
            check);
    }

    protected final int survivalBuildPiece(String piece, ItemStack trigger, int horizontalOffset, int verticalOffset,
        int depthOffset, int elementsBudget, ISurvivalBuildEnvironment env, boolean check, boolean checkIfPlaced) {
        int built = survivalBuildPiece(
            piece,
            trigger,
            horizontalOffset,
            verticalOffset,
            depthOffset,
            elementsBudget,
            env,
            check);
        if (checkIfPlaced && built > 0) structureLogic.checkStructure(getBaseMetaTileEntity());
        return built;
    }

    @Override
    public void writeToStream(ByteBuf buffer) {
        super.writeToStream(buffer);
        buffer.writeFloat(center.x);
        buffer.writeFloat(center.y);
        buffer.writeFloat(center.z);
        buffer.writeInt(structureRadius);
        buffer.writeByte(mExtendedFacing.getIndex());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readFromStream(ByteBuf buffer) {
        super.readFromStream(buffer);
        center.x = buffer.readFloat();
        center.y = buffer.readFloat();
        center.z = buffer.readFloat();

        int oldRadius = structureRadius;
        structureRadius = buffer.readInt();
        var activitySoundLoop = tryGetComponent(IActivitySoundLoopComponent.KEY);
        if (oldRadius != structureRadius && activitySoundLoop != null) {
            activitySoundLoop.restartActivitySound();
        }

        mExtendedFacing = ExtendedFacing.byIndex(buffer.readByte());
    }

    public static class StructureSize {

        public int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        public int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        public float centerX, centerY, centerZ;
        public int radius;
    }

    private class StructureCenterWalker implements IStructureWalker<T> {

        private StructureSize size;

        public StructureSize finish() {
            // When `size` is null, for whatever reason the piece method was never called, which never called [#visit].
            // We'll just fall back to the default behaviour in this case - play the sound at the center with the
            // default radius.
            // This usually happens when a multi has its own structure check logic.
            if (this.size == null) return null;
            var result = size;
            this.size = null;

            result.centerX = (result.minX + result.maxX) * 0.5f;
            result.centerY = (result.minY + result.maxY) * 0.5f;
            result.centerZ = (result.minZ + result.maxZ) * 0.5f;

            result.radius = GTUtility
                .max(result.maxX - result.minX, result.maxY - result.minY, result.maxZ - result.minZ) / 2;

            return result;
        }

        @Override
        public boolean visit(IStructureElement<T> element, World world, int x, int y, int z, int a, int b, int c) {
            if (size == null) {
                size = new StructureSize();
            }

            size.minX = Math.min(size.minX, x);
            size.minY = Math.min(size.minY, y);
            size.minZ = Math.min(size.minZ, z);

            size.maxX = Math.max(size.maxX, x);
            size.maxY = Math.max(size.maxY, y);
            size.maxZ = Math.max(size.maxZ, z);

            return true;
        }
    }
}
