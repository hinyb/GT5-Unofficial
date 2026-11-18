package gregtech.api.ec.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IActivitySoundLoopComponent;
import gregtech.api.ec.IComponent;
import gregtech.api.ec.IPollutionLogicComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IRecipePollingComponent;
import gregtech.api.ec.multiblock.IBatchConfigComponent;
import gregtech.api.ec.multiblock.ICoilLogicComponent;
import gregtech.api.ec.multiblock.IDualInputLogicComponent;
import gregtech.api.ec.multiblock.IECMultiblock;
import gregtech.api.ec.multiblock.IInputSeparationComponent;
import gregtech.api.ec.multiblock.IMEFluidInputLogicComponent;
import gregtech.api.ec.multiblock.IMEItemInputLogicComponent;
import gregtech.api.ec.multiblock.IMaintenanceLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyOutputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockFluidOutputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockItemInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockItemOutputLogicComponent;
import gregtech.api.ec.multiblock.IPowerSetupComponent;
import gregtech.api.ec.multiblock.IRecipeEfficiencyLogicComponent;
import gregtech.api.ec.multiblock.IRecipeLockingComponent;
import gregtech.api.ec.multiblock.IVoidingConfigComponent;
import gregtech.api.enums.SoundResource;
import gregtech.api.logic.ProcessingLogic;
import gregtech.common.ec.multiblock.ActivitySoundLoopComponent;
import gregtech.common.ec.multiblock.BatchConfigComponent;
import gregtech.common.ec.multiblock.CoilLogicComponent;
import gregtech.common.ec.multiblock.ColorRecipeCheckComponent;
import gregtech.common.ec.multiblock.ControllerToolWearComponent;
import gregtech.common.ec.multiblock.DualInputLogicComponent;
import gregtech.common.ec.multiblock.EnergyConsumeComponent;
import gregtech.common.ec.multiblock.EnergyInputLogicComponent;
import gregtech.common.ec.multiblock.EnergyOutputLogicComponent;
import gregtech.common.ec.multiblock.FluidInputLogicComponent;
import gregtech.common.ec.multiblock.FluidOutputLogicComponent;
import gregtech.common.ec.multiblock.InputSeparationComponent;
import gregtech.common.ec.multiblock.ItemInputLogicComponent;
import gregtech.common.ec.multiblock.ItemOutputLogicComponent;
import gregtech.common.ec.multiblock.MEFluidInputLogicComponent;
import gregtech.common.ec.multiblock.MEItemInputLogicComponent;
import gregtech.common.ec.multiblock.MaintenanceLogicComponent;
import gregtech.common.ec.multiblock.ParallelLogicComponent;
import gregtech.common.ec.multiblock.PollutionEmitterComponent;
import gregtech.common.ec.multiblock.PollutionLogicComponent;
import gregtech.common.ec.multiblock.PowerOverflowCheckComponent;
import gregtech.common.ec.multiblock.PowerSetupComponent;
import gregtech.common.ec.multiblock.RandomMaintenanceIssueComponent;
import gregtech.common.ec.multiblock.RecipeEfficiencyLogicComponent;
import gregtech.common.ec.multiblock.RecipeLockingComponent;
import gregtech.common.ec.multiblock.RecipeLogicComponent;
import gregtech.common.ec.multiblock.RecipePollingComponent;
import gregtech.common.ec.multiblock.RecipeSoundComponent;
import gregtech.common.ec.multiblock.VoidingConfigComponent;

public class MultiblockComponentBuilder {

    protected @NotNull BooleanSupplier filtersFluid = () -> true;
    private @Nullable PollutionEmitterComponent.IPollutionCalculator pollutionCalculator = null;
    private final ComponentSlot<IDualInputLogicComponent, DualInputLogicComponent> dualInputSlot = new ComponentSlot<>(
        true,
        DualInputLogicComponent::new);
    private final ComponentSlot<IMultiblockItemInputLogicComponent, ItemInputLogicComponent> itemInputSlot = new ComponentSlot<>(
        true,
        ItemInputLogicComponent::new);
    private final ComponentSlot<IMEItemInputLogicComponent, MEItemInputLogicComponent> meItemInputSlot = new ComponentSlot<>(
        true,
        MEItemInputLogicComponent::new);
    private final ComponentSlot<IMultiblockFluidInputLogicComponent, FluidInputLogicComponent> fluidInputSlot = new ComponentSlot<>(
        true,
        () -> new FluidInputLogicComponent(filtersFluid));
    private final ComponentSlot<IMEFluidInputLogicComponent, MEFluidInputLogicComponent> meFluidInputSlot = new ComponentSlot<>(
        true,
        () -> new MEFluidInputLogicComponent(filtersFluid));
    private final ComponentSlot<IMultiblockItemOutputLogicComponent, ItemOutputLogicComponent> itemOutputSlot = new ComponentSlot<>(
        true,
        ItemOutputLogicComponent::new);
    private final ComponentSlot<IMultiblockFluidOutputLogicComponent, FluidOutputLogicComponent> fluidOutputSlot = new ComponentSlot<>(
        true,
        FluidOutputLogicComponent::new);
    private final ComponentSlot<IMultiblockEnergyInputLogicComponent, EnergyInputLogicComponent> energyInputSlot = new ComponentSlot<>(
        true,
        EnergyInputLogicComponent::new);
    private final ComponentSlot<IMultiblockEnergyOutputLogicComponent, EnergyOutputLogicComponent> energyOutputSlot = new ComponentSlot<>(
        false,
        EnergyOutputLogicComponent::new);
    private final ComponentSlot<IPollutionLogicComponent, PollutionLogicComponent> pollutionLogicSlot = new ComponentSlot<>(
        false,
        () -> pollutionCalculator != null ? new PollutionLogicComponent() : null);
    private final ComponentSlot<IPowerSetupComponent, PowerSetupComponent> powerSetupSlot = new ComponentSlot<>(
        true,
        PowerSetupComponent::new);
    private final ComponentSlot<IRecipeEfficiencyLogicComponent, RecipeEfficiencyLogicComponent> efficiencySlot = new ComponentSlot<>(
        true,
        RecipeEfficiencyLogicComponent::new);
    private final ComponentSlot<ColorRecipeCheckComponent, ColorRecipeCheckComponent> recipeCheckSlot = new ComponentSlot<>(
        true,
        ColorRecipeCheckComponent::new);
    private final ComponentSlot<IBatchConfigComponent, BatchConfigComponent> batchModeSlot = new ComponentSlot<>(
        false,
        BatchConfigComponent::new);
    private final ComponentSlot<IInputSeparationComponent, InputSeparationComponent> inputSeparationSlot = new ComponentSlot<>(
        false,
        InputSeparationComponent::new);
    private final ComponentSlot<IVoidingConfigComponent, VoidingConfigComponent> voidProtectionSlot = new ComponentSlot<>(
        false,
        VoidingConfigComponent::new);
    private final ComponentSlot<IRecipeLockingComponent, RecipeLockingComponent> recipeLockingSlot = new ComponentSlot<>(
        false,
        RecipeLockingComponent::new);
    private final ComponentSlot<ICoilLogicComponent, CoilLogicComponent> coilSlot = new ComponentSlot<>(
        false,
        CoilLogicComponent::new);
    private final ComponentSlot<IRecipePollingComponent, RecipePollingComponent> recipePollingSlot = new ComponentSlot<>(
        true,
        RecipePollingComponent::new);
    private final ComponentSlot<IMaintenanceLogicComponent, MaintenanceLogicComponent> maintenanceSlot = new ComponentSlot<>(
        true,
        MaintenanceLogicComponent::new);

    public MultiblockComponentBuilder apply(@NotNull Consumer<MultiblockComponentBuilder> preset) {
        preset.accept(this);
        return this;
    }

    public MultiblockComponentBuilder enableStandardQoL() {
        batchModeSlot.enable();
        voidProtectionSlot.enable();
        inputSeparationSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder noFluidIO() {
        return disableFluidInput().disableFluidOutput();
    }

    public MultiblockComponentBuilder noItemIO() {
        return disableItemInput().disableItemOutput();
    }

    public MultiblockComponentBuilder noInputs() {
        return disableDualInput().disableItemInput()
            .disableFluidInput();
    }

    public MultiblockComponentBuilder noOutputs() {
        return disableItemOutput().disableFluidOutput();
    }

    public MultiblockComponentBuilder noIO() {
        return noInputs().noOutputs()
            .disableRecipePolling();
    }

    public MultiblockComponentBuilder withFluidFiltering(@NotNull BooleanSupplier filtersFluid) {
        this.filtersFluid = filtersFluid;
        return this;
    }

    public MultiblockComponentBuilder disableDualInput() {
        dualInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setDualInput(@Nullable IDualInputLogicComponent dualInput) {
        dualInputSlot.set(dualInput);
        return this;
    }

    public MultiblockComponentBuilder configureDualInput(@NotNull Consumer<DualInputLogicComponent> consumer) {
        dualInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableItemInput() {
        itemInputSlot.disable();
        meItemInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setItemInput(@Nullable IMultiblockItemInputLogicComponent itemInput) {
        itemInputSlot.set(itemInput);
        return this;
    }

    public MultiblockComponentBuilder configureItemInput(@NotNull Consumer<ItemInputLogicComponent> consumer) {
        itemInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableMEItemInput() {
        meItemInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setMEItemInput(@Nullable IMEItemInputLogicComponent itemInput) {
        meItemInputSlot.set(itemInput);
        return this;
    }

    public MultiblockComponentBuilder configureMEItemInput(@NotNull Consumer<MEItemInputLogicComponent> consumer) {
        meItemInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableFluidInput() {
        fluidInputSlot.disable();
        meFluidInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setFluidInput(@Nullable IMultiblockFluidInputLogicComponent fluidInput) {
        fluidInputSlot.set(fluidInput);
        return this;
    }

    public MultiblockComponentBuilder configureFluidInput(@NotNull Consumer<FluidInputLogicComponent> consumer) {
        fluidInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableMEFluidInput() {
        meFluidInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setMEFluidInput(@Nullable IMEFluidInputLogicComponent fluidInput) {
        meFluidInputSlot.set(fluidInput);
        return this;
    }

    public MultiblockComponentBuilder configureMEFluidInput(@NotNull Consumer<MEFluidInputLogicComponent> consumer) {
        meFluidInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableItemOutput() {
        itemOutputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setItemOutput(@Nullable IMultiblockItemOutputLogicComponent itemOutput) {
        itemOutputSlot.set(itemOutput);
        return this;
    }

    public MultiblockComponentBuilder configureItemOutput(@NotNull Consumer<ItemOutputLogicComponent> consumer) {
        itemOutputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableFluidOutput() {
        fluidOutputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setFluidOutput(@Nullable IMultiblockFluidOutputLogicComponent fluidOutput) {
        fluidOutputSlot.set(fluidOutput);
        return this;
    }

    public MultiblockComponentBuilder configureFluidOutput(@NotNull Consumer<FluidOutputLogicComponent> consumer) {
        fluidOutputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableEnergyInput() {
        energyInputSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setEnergyInput(@Nullable IMultiblockEnergyInputLogicComponent energyInput) {
        energyInputSlot.set(energyInput);
        return this;
    }

    public MultiblockComponentBuilder configureEnergyInput(@NotNull Consumer<EnergyInputLogicComponent> consumer) {
        energyInputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableEnergyOutput() {
        energyOutputSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder setEnergyOutput(@Nullable IMultiblockEnergyOutputLogicComponent energyOutput) {
        energyOutputSlot.set(energyOutput);
        return this;
    }

    public MultiblockComponentBuilder configureEnergyOutput(@NotNull Consumer<EnergyOutputLogicComponent> consumer) {
        energyOutputSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disablePollutionLogic() {
        pollutionLogicSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setPollutionLogic(@Nullable IPollutionLogicComponent pollutionLogic) {
        pollutionLogicSlot.set(pollutionLogic);
        return this;
    }

    public MultiblockComponentBuilder configurePollutionLogic(@NotNull Consumer<PollutionLogicComponent> consumer) {
        pollutionLogicSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disablePowerSetup() {
        powerSetupSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setPowerSetup(@Nullable IPowerSetupComponent powerSetup) {
        powerSetupSlot.set(powerSetup);
        return this;
    }

    public MultiblockComponentBuilder configurePower(@NotNull Consumer<PowerSetupComponent> consumer) {
        powerSetupSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableEfficiency() {
        efficiencySlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setEfficiency(@Nullable IRecipeEfficiencyLogicComponent efficiency) {
        efficiencySlot.set(efficiency);
        return this;
    }

    public MultiblockComponentBuilder configureEfficiency(@NotNull Consumer<RecipeEfficiencyLogicComponent> consumer) {
        efficiencySlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder disableRecipeCheck() {
        recipeCheckSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setRecipeCheck(@Nullable ColorRecipeCheckComponent recipeCheck) {
        recipeCheckSlot.set(recipeCheck);
        return this;
    }

    public MultiblockComponentBuilder configureRecipeCheck(@NotNull Consumer<ColorRecipeCheckComponent> consumer) {
        recipeCheckSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableBatchMode() {
        batchModeSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableBatchMode() {
        batchModeSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setBatchMode(@Nullable IBatchConfigComponent batchConfig) {
        batchModeSlot.set(batchConfig);
        return this;
    }

    public MultiblockComponentBuilder configureBatchMode(@NotNull Consumer<BatchConfigComponent> consumer) {
        batchModeSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableInputSeparation() {
        inputSeparationSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableInputSeparation() {
        inputSeparationSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setInputSeparation(@Nullable IInputSeparationComponent inputSeparation) {
        inputSeparationSlot.set(inputSeparation);
        return this;
    }

    public MultiblockComponentBuilder configureInputSeparation(@NotNull Consumer<InputSeparationComponent> consumer) {
        inputSeparationSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableVoidProtection() {
        voidProtectionSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableVoidProtection() {
        voidProtectionSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setVoidProtection(@Nullable IVoidingConfigComponent voidProtection) {
        voidProtectionSlot.set(voidProtection);
        return this;
    }

    public MultiblockComponentBuilder configureVoidProtection(@NotNull Consumer<VoidingConfigComponent> consumer) {
        voidProtectionSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableRecipeLocking() {
        recipeLockingSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableRecipeLocking() {
        recipeLockingSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setRecipeLocking(@Nullable IRecipeLockingComponent recipeLocking) {
        recipeLockingSlot.set(recipeLocking);
        return this;
    }

    public MultiblockComponentBuilder configureRecipeLocking(@NotNull Consumer<RecipeLockingComponent> consumer) {
        recipeLockingSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableCoils() {
        coilSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableCoils() {
        coilSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setCoil(@Nullable ICoilLogicComponent coilLogic) {
        coilSlot.set(coilLogic);
        return this;
    }

    public MultiblockComponentBuilder configureCoil(@NotNull Consumer<CoilLogicComponent> consumer) {
        coilSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder enableRecipePolling() {
        recipePollingSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disableRecipePolling() {
        recipePollingSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder setRecipePolling(@Nullable IRecipePollingComponent recipePolling) {
        recipePollingSlot.set(recipePolling);
        return this;
    }

    public MultiblockComponentBuilder configureRecipePolling(@NotNull Consumer<RecipePollingComponent> consumer) {
        recipePollingSlot.configure(consumer);
        return this;
    }

    private boolean recipeLogicEnabled = true;
    private @Nullable IRecipeLogicComponent customRecipeLogic = null;
    private @Nullable Consumer<RecipeLogicComponent> recipeLogicConfigurator = null;

    public MultiblockComponentBuilder configureRecipe(@NotNull Consumer<RecipeLogicComponent> consumer) {
        recipeLogicEnabled = true;
        recipeLogicConfigurator = recipeLogicConfigurator == null ? consumer
            : recipeLogicConfigurator.andThen(consumer);
        return this;
    }

    public MultiblockComponentBuilder setRecipeLogic(@NotNull IRecipeLogicComponent customRecipeLogic) {
        this.recipeLogicEnabled = true;
        this.customRecipeLogic = customRecipeLogic;
        return this;
    }

    public MultiblockComponentBuilder disableRecipeLogic() {
        this.recipeLogicEnabled = false;
        this.customRecipeLogic = null;
        return this;
    }

    private boolean energyConsume = true;

    public MultiblockComponentBuilder disableEnergyConsume() {
        energyConsume = false;
        return this;
    }

    private boolean powerOverflowCheck = true;

    public MultiblockComponentBuilder enableExtendedPower() {
        powerOverflowCheck = false;
        return this;
    }

    private boolean randomMaintenanceDamage = true;
    private @Nullable ControllerToolWearComponent toolWearComponent = null;
    private @Nullable IActivitySoundLoopComponent clientSoundLoop = null;
    private @Nullable ParallelLogicComponent.IMaxParallelProvider maxParallelProvider = null;
    private @Nullable RecipeSoundComponent recipeSound = null;
    private final @NotNull List<IComponent> extraComponents = new ArrayList<>();

    public MultiblockComponentBuilder enableMaintenance() {
        this.maintenanceSlot.enable();
        this.randomMaintenanceDamage = true;
        return this;
    }

    public MultiblockComponentBuilder disableMaintenance() {
        this.maintenanceSlot.disable();
        this.randomMaintenanceDamage = false;
        return this;
    }

    public MultiblockComponentBuilder setMaintenance(@Nullable IMaintenanceLogicComponent maintenance) {
        this.maintenanceSlot.set(maintenance);
        return this;
    }

    public MultiblockComponentBuilder configureMaintenance(@NotNull Consumer<MaintenanceLogicComponent> consumer) {
        this.maintenanceSlot.configure(consumer);
        return this;
    }

    public MultiblockComponentBuilder setMaintenanceIssues(int allowMasks) {
        if (allowMasks == 0) {
            return disableMaintenance();
        }
        return configureMaintenance(comp -> comp.setAllowMasks(allowMasks));
    }

    public MultiblockComponentBuilder disableRandomMaintenanceDamage() {
        this.randomMaintenanceDamage = false;
        return this;
    }

    public MultiblockComponentBuilder enablePollution(int pollutionPerSecond) {
        this.pollutionCalculator = _ -> pollutionPerSecond;
        this.pollutionLogicSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder enablePollution(
        @NotNull PollutionEmitterComponent.IPollutionCalculator pollutionCalculator) {
        this.pollutionCalculator = pollutionCalculator;
        this.pollutionLogicSlot.enable();
        return this;
    }

    public MultiblockComponentBuilder disablePollution() {
        this.pollutionCalculator = null;
        this.pollutionLogicSlot.disable();
        return this;
    }

    public MultiblockComponentBuilder enableToolWear(@NotNull ControllerToolWearComponent.IToolValidator toolValidator,
        @NotNull ControllerToolWearComponent.IToolDamageCalculator toolDamageCalculator) {
        this.toolWearComponent = new ControllerToolWearComponent(toolValidator, toolDamageCalculator);
        return this;
    }

    public MultiblockComponentBuilder disableToolWear() {
        this.toolWearComponent = null;
        return this;
    }

    public MultiblockComponentBuilder setActivitySound(@NotNull SoundResource sound) {
        clientSoundLoop = new ActivitySoundLoopComponent(sound);
        return this;
    }

    public MultiblockComponentBuilder setActivitySound(@NotNull IActivitySoundLoopComponent comp) {
        clientSoundLoop = comp;
        return this;
    }

    public MultiblockComponentBuilder enableParallel(int maxParallel) {
        this.maxParallelProvider = () -> maxParallel;
        return this;
    }

    public MultiblockComponentBuilder enableParallel(
        @NotNull ParallelLogicComponent.IMaxParallelProvider maxParallelProvider) {
        this.maxParallelProvider = maxParallelProvider;
        return this;
    }

    public MultiblockComponentBuilder disableParallel() {
        this.maxParallelProvider = null;
        return this;
    }

    public MultiblockComponentBuilder setRecipeSound(@NotNull SoundResource sound, int interval) {
        this.recipeSound = new RecipeSoundComponent(sound, interval);
        return this;
    }

    public MultiblockComponentBuilder addComponent(@NotNull IComponent component) {
        this.extraComponents.add(component);
        return this;
    }

    public void build(@NotNull IECMultiblock multiblock, @NotNull Supplier<@NotNull ProcessingLogic> logicSupplier,
        @NotNull RecipeLogicComponent.IRecipeMapProvider recipeMapProvider) {
        dualInputSlot.registerTo(multiblock);
        itemInputSlot.registerTo(multiblock);
        meItemInputSlot.registerTo(multiblock);
        fluidInputSlot.registerTo(multiblock);
        meFluidInputSlot.registerTo(multiblock);
        itemOutputSlot.registerTo(multiblock);
        fluidOutputSlot.registerTo(multiblock);
        energyInputSlot.registerTo(multiblock);
        energyOutputSlot.registerTo(multiblock);
        pollutionLogicSlot.registerTo(multiblock);
        efficiencySlot.registerTo(multiblock);
        maintenanceSlot.registerTo(multiblock);
        if (maintenanceSlot.isEnabled() && randomMaintenanceDamage) {
            multiblock.addComponent(new RandomMaintenanceIssueComponent());
        }
        if (pollutionCalculator != null) {
            multiblock.addComponent(new PollutionEmitterComponent(pollutionCalculator));
        }
        if (toolWearComponent != null) {
            multiblock.addComponent(toolWearComponent);
        }
        if (energyConsume && (energyInputSlot.isEnabled() || energyOutputSlot.isEnabled())) {
            multiblock.addComponent(new EnergyConsumeComponent());
        }
        if (recipeLogicEnabled) {
            recipePollingSlot.registerTo(multiblock);
            if (customRecipeLogic != null) {
                multiblock.addComponent(customRecipeLogic);
            } else {
                var recipeComp = new RecipeLogicComponent(logicSupplier.get(), recipeMapProvider);
                if (recipeLogicConfigurator != null) {
                    recipeLogicConfigurator.accept(recipeComp);
                }
                multiblock.addComponent(recipeComp);
                powerSetupSlot.registerTo(multiblock);
                recipeCheckSlot.registerTo(multiblock);
                if (powerOverflowCheck) {
                    multiblock.addComponent(new PowerOverflowCheckComponent());
                }
            }
        }
        if (maxParallelProvider != null) {
            multiblock.addComponent(new ParallelLogicComponent(maxParallelProvider));
        }
        batchModeSlot.registerTo(multiblock);
        inputSeparationSlot.registerTo(multiblock);
        recipeLockingSlot.registerTo(multiblock);
        voidProtectionSlot.registerTo(multiblock);
        coilSlot.registerTo(multiblock);
        if (recipeSound != null) {
            multiblock.addComponent(recipeSound);
        }
        if (clientSoundLoop != null) {
            multiblock.addComponent(clientSoundLoop);
        }
        for (var comp : extraComponents) {
            multiblock.addComponent(comp);
        }
    }

    protected static class ComponentSlot<T extends IComponent, D extends T> {

        private boolean enabled;
        private @NotNull Supplier<? extends D> defaultFactory;
        private @Nullable T customInstance;
        private @Nullable Consumer<D> configurator;

        public ComponentSlot(boolean defaultEnabled, @NotNull Supplier<? extends D> defaultFactory) {
            this.enabled = defaultEnabled;
            this.defaultFactory = defaultFactory;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
            customInstance = null;
        }

        public void set(@Nullable T instance) {
            this.customInstance = instance;
            this.enabled = instance != null;
        }

        public <C extends T> void configure(@NotNull Consumer<D> consumer) {
            enable();
            configurator = configurator == null ? consumer : configurator.andThen(consumer);
        }

        public void registerTo(@NotNull IECMultiblock multiblock) {
            if (!enabled) return;
            if (customInstance != null) {
                multiblock.addComponent(customInstance);
                return;
            }
            D comp = defaultFactory.get();
            if (comp != null) {
                if (configurator != null) {
                    configurator.accept(comp);
                }
                multiblock.addComponent(comp);
            }
        }
    }
}
