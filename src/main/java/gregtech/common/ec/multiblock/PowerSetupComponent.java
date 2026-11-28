package gregtech.common.ec.multiblock;

import static gregtech.api.ec.component.IProcessingLogicRecipeComponent.POST_RECIPE_CHECK;
import static gregtech.api.ec.component.IProcessingLogicRecipeComponent.PROCESSING_LOGIC_SETUP;

import java.util.function.ToLongFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IProcessingLogicRecipeComponent;
import gregtech.api.ec.component.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.component.multiblock.IPowerSetupComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class PowerSetupComponent extends BaseMultiblockComponent implements IPowerSetupComponent {

    private ToLongFunction<IMultiblockEnergyInputLogicComponent> voltageProvider = IMultiblockEnergyInputLogicComponent::getAverageInputVoltage;
    private ToLongFunction<IMultiblockEnergyInputLogicComponent> amperageProvider = energyInput -> {
        boolean useSingleAmp = !energyInput.isDebugEnergyPresent() && energyInput.getNormalEnergyHatches()
            .size() == 1
            && energyInput.getExoticEnergyHatches()
                .isEmpty();
        return useSingleAmp ? 1 : energyInput.getMaxInputAmps();
    };
    private boolean amperageOC = true;
    private int maxTierSkips = 1;
    private boolean extendedPower = false;

    public PowerSetupComponent setAmperageOC(boolean amperageOC) {
        this.amperageOC = amperageOC;
        return this;
    }

    public PowerSetupComponent setVoltageProvider(
        @NotNull ToLongFunction<IMultiblockEnergyInputLogicComponent> voltageProvider) {
        this.voltageProvider = voltageProvider;
        return this;
    }

    public PowerSetupComponent setAmperageProvider(
        @NotNull ToLongFunction<IMultiblockEnergyInputLogicComponent> amperageProvider) {
        this.amperageProvider = amperageProvider;
        return this;
    }

    public PowerSetupComponent setMaxTierSkips(int maxTierSkips) {
        this.maxTierSkips = maxTierSkips;
        return this;
    }

    public PowerSetupComponent setExtendedPower(boolean extendedPower) {
        this.extendedPower = extendedPower;
        return this;
    }

    public void setupProcessingLogic(ProcessingLogic logic) {
        if (energyInputLogic != null) {
            logic.setAvailableVoltage(voltageProvider.applyAsLong(energyInputLogic));
            logic.setAvailableAmperage(amperageProvider.applyAsLong(energyInputLogic));
        }
        logic.setAmperageOC(amperageOC);
        logic.setMaxTierSkips(maxTierSkips);
    }

    private @Nullable IMultiblockEnergyInputLogicComponent energyInputLogic;

    @Override
    public void onComponentsReady() {
        energyInputLogic = tryGetComponent(IMultiblockEnergyInputLogicComponent.KEY);
        getComponent(IProcessingLogicRecipeComponent.KEY);
    }

    public @NotNull CheckRecipeResult onPostRecipeCheck(@NotNull CheckRecipeResult result,
        @NotNull ProcessingLogic logic) {
        if (!extendedPower && result.wasSuccessful() && logic.getCalculatedEut() > Integer.MAX_VALUE) {
            return CheckRecipeResultRegistry.POWER_OVERFLOW;
        }
        return result;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(POST_RECIPE_CHECK, this::onPostRecipeCheck);
        connect(PROCESSING_LOGIC_SETUP, this::setupProcessingLogic);
    }
}
