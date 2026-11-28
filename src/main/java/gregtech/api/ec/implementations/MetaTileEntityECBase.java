package gregtech.api.ec.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.ComponentContainer;
import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.Connection;
import gregtech.api.ec.IComponent;
import gregtech.api.ec.IECMachine;
import gregtech.api.ec.OrderedSectionBuilder;
import gregtech.api.ec.Signal;
import gregtech.api.ec.SignalKey;
import gregtech.api.ec.component.IActivitySoundLoopComponent;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.ISavableComponent;
import gregtech.api.ec.signal.IOnClientPostTick;
import gregtech.api.ec.signal.IOnServerPostTick;
import gregtech.api.ec.signal.IWailaBodyProvider;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public abstract class MetaTileEntityECBase extends MetaTileEntity implements IECMachine {

    public MetaTileEntityECBase(int aID, String aBasicName, String aRegionalName, int aInvSlotCount) {
        super(aID, aBasicName, aRegionalName, aInvSlotCount);
    }

    public MetaTileEntityECBase(String aName, int aInvSlotCount) {
        super(aName, aInvSlotCount);
    }

    private final ComponentContainer container = new ComponentContainer(this, this::onComponentsReady);

    protected final void ensureComponentsInitialized() {
        if (!container.isInitialized()) initComponents();
    }

    protected @Nullable Signal<IOnServerPostTick> serverPostTicks;
    protected @Nullable Signal<IOnClientPostTick> clientPostTicks;

    protected void initComponents() {
        serverPostTicks = container.signal(ON_SERVER_POST_TICK);
        clientPostTicks = container.signal(ON_CLIENT_POST_TICK);
        clientPostTicks.watchActive(this, 0, (_, active) -> {
            needsClientTick = active;
            if (container.isReady()) {
                var igte = getBaseMetaTileEntity();
                if (igte != null && igte.isClientSide()) {
                    // update cached mNeedsClientTick
                    igte.setMetaTileEntity(this);
                    if (needsClientTick) {
                        igte.enableTicking();
                    }
                }
            }
        });
        container.initialize(this::registerComponents);
    }

    protected abstract void registerComponents();

    private boolean needsClientTick = false;

    protected void onComponentsReady() {}

    @Override
    public final IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        var instance = createNewMetaEntity(aTileEntity);
        instance.ensureComponentsInitialized();
        return instance;
    }

    protected abstract @NotNull MetaTileEntityECBase createNewMetaEntity(IGregTechTileEntity aTileEntity);

    @Override
    public final <C extends IComponent> @Nullable C tryGetComponent(@NotNull ComponentKey.UniqueKey<C> key) {
        ensureComponentsInitialized();
        return container.tryGetComponent(key);
    }

    @Override
    public final <C extends IComponent> boolean hasComponent(@NotNull ComponentKey<C> key) {
        ensureComponentsInitialized();
        return container.hasComponent(key);
    }

    @Override
    public final <C extends IComponent> @NotNull C[] getComponents(@NotNull ComponentKey.ListKey<C> key) {
        ensureComponentsInitialized();
        return container.getComponents(key);
    }

    @Override
    public final <C extends IComponent> IECMachine addComponent(@NotNull C component) {
        ensureComponentsInitialized();
        container.addComponent(component);
        return this;
    }

    @Override
    public final <C extends IComponent> boolean removeComponent(@NotNull C component) {
        if (!container.isInitialized()) return false;
        return container.removeComponent(component);
    }

    @Override
    public final <C extends IComponent> boolean removeComponent(@NotNull ComponentKey.ListKey<C> key) {
        if (!container.isInitialized()) return false;
        return container.removeComponent(key);
    }

    @Override
    public final <T> @NotNull Signal<T> signal(@NotNull SignalKey<T> key) {
        ensureComponentsInitialized();
        return container.signal(key);
    }

    @Override
    public final <T> @NotNull T[] listeners(@NotNull SignalKey<T> key) {
        ensureComponentsInitialized();
        return container.listeners(key);
    }

    @Override
    public final boolean hasListeners(@NotNull SignalKey<?> key) {
        ensureComponentsInitialized();
        return container.hasListeners(key);
    }

    protected <T> @NotNull Connection connect(@NotNull SignalKey<T> key, @NotNull T listener) {
        return connect(key, 0, listener);
    }

    protected <T> @NotNull Connection connect(@NotNull SignalKey<T> key, int priority, @NotNull T listener) {
        return signal(key).connect(this, priority, listener);
    }

    @Override
    public final boolean onWrenchRightClick(ForgeDirection side, ForgeDirection wrenchingSide,
        EntityPlayer entityPlayer, float aX, float aY, float aZ, ItemStack aTool) {
        for (var listener : listeners(ON_WRENCH_CLICK)) {
            if (listener.onToolRightClick(side, wrenchingSide, entityPlayer, aX, aY, aZ, aTool)) return true;
        }
        return super.onWrenchRightClick(side, wrenchingSide, entityPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public final void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        for (var listener : listeners(ON_SCREWDRIVER_CLICK)) {
            if (listener.onToolRightClick(side, null, aPlayer, aX, aY, aZ, aTool)) return;
        }
        super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public final boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {
        for (var listener : listeners(ON_WIRE_CUTTER_CLICK)) {
            if (listener.onToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool)) return true;
        }
        return super.onWireCutterRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public final boolean onSolderingToolRightClick(ForgeDirection side, ForgeDirection wrenchingSide,
        EntityPlayer aPlayer, float aX, float aY, float aZ, ItemStack aTool) {
        for (var listener : listeners(ON_SOLDERING_TOOL_CLICK)) {
            if (listener.onToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool)) return true;
        }
        return super.onSolderingToolRightClick(side, wrenchingSide, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public boolean needsClientTick() {
        return needsClientTick;
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
    public void onDisableWorking() {
        for (var listener : listeners(ON_WORK_STATE_CHANGE)) {
            listener.onWorkStateChanged(false);
        }
    }

    @Override
    public void onEnableWorking() {
        for (var listener : listeners(ON_WORK_STATE_CHANGE)) {
            listener.onWorkStateChanged(true);
        }
    }

    @Override
    public final void doSound(byte aIndex, double aX, double aY, double aZ) {
        super.doSound(aIndex, aX, aY, aZ);
        for (var listener : listeners(ON_DO_SOUND)) {
            if (listener.onDoSound(aIndex, aX, aY, aZ)) return;
        }
    }

    @Override
    public void startSoundLoop(byte aIndex, double aX, double aY, double aZ) {
        super.startSoundLoop(aIndex, aX, aY, aZ);
        for (var listener : listeners(ON_START_SOUND_LOOP)) {
            listener.onStartSoundLoop(aIndex, aX, aY, aZ);
        }
    }

    private boolean lastActive = false;

    private void updateActiveState(@NotNull IGregTechTileEntity igte, boolean active) {
        if (lastActive == active) return;
        lastActive = active;
        for (var listener : listeners(ON_ACTIVE_STATE_CHANGE)) {
            listener.onActiveStateChanged(igte, active);
        }
    }

    @Override
    public void onSetActive(boolean active) {
        super.onSetActive(active);
        var igte = getBaseMetaTileEntity();
        if (igte != null && igte.isServerSide()) {
            updateActiveState(igte, active);
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        for (var listener : listeners(ON_FIRST_TICK)) {
            listener.onFirstTick(aBaseMetaTileEntity);
        }
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
            for (var listener : listeners(ON_SERVER_TICK_FAIL)) {
                listener.onServerTickFail(aBaseMetaTileEntity, aTick);
            }
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        for (var listener : listeners(ON_MACHINE_REMOVAL)) {
            listener.onMachineRemoval();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        for (var listener : listeners(ON_MACHINE_UNLOAD)) {
            listener.onMachineUnload();
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        for (var component : getComponents(ISavableComponent.KEY)) {
            component.saveNBTData(aNBT);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        for (var component : getComponents(ISavableComponent.KEY)) {
            component.loadNBTData(aNBT);
        }
        var igte = getBaseMetaTileEntity();
        if (igte != null) {
            lastActive = igte.isActive();
        }
    }

    @Override
    public int getRandomNumber(int aRange) {
        return getBaseMetaTileEntity().getRandomNumber(aRange);
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
    public boolean isActive() {
        var igte = getBaseMetaTileEntity();
        if (igte == null) return false;
        return igte.isActive();
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide()) {
            for (var listener : serverPostTicks.listeners()) {
                listener.onServerPostTick(aBaseMetaTileEntity, aTick);
            }
        } else if (!needsClientTick) {
            aBaseMetaTileEntity.tryDisableTicking();
        } else {
            for (var listener : clientPostTicks.listeners()) {
                listener.onClientPostTick(aBaseMetaTileEntity, aTick);
            }
        }
    }

    @Override
    public String[] getInfoData() {
        OrderedSectionBuilder<Consumer<List<String>>> builder = new OrderedSectionBuilder<>();
        for (var listener : listeners(SCANNER_INFO_DATA_PROVIDER)) {
            listener.appendInfoData(builder);
        }
        List<String> infoData = new ArrayList<>();
        builder.forEach(consumer -> consumer.accept(infoData));
        return infoData.toArray(new String[0]);
    }

    @Override
    public Map<String, String> getInfoMap() {
        Map<String, String> infoMap = new HashMap<>();
        for (var listener : listeners(SCANNER_INFO_MAP_PROVIDER)) {
            listener.appendInfoMap(infoMap);
        }
        return infoMap;
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        for (var listener : listeners(WAILA_DATA_PROVIDER)) {
            listener.appendWailaNBTData(player, tile, tag, world, x, y, z);
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final NBTTagCompound tag = accessor.getNBTData();
        OrderedSectionBuilder<IWailaBodyProvider.WailaBodyProvider> builder = new OrderedSectionBuilder<>();
        for (var listener : listeners(WAILA_BODY_PROVIDER)) {
            listener.appendWailaBody(builder);
        }
        var ctx = new IWailaBodyProvider.WailaBodyContext(itemStack, tag, accessor, config);
        builder.forEach(provider -> provider.append(ctx, currentTip));
        super.getWailaBody(itemStack, currentTip, accessor, config);
    }
}
