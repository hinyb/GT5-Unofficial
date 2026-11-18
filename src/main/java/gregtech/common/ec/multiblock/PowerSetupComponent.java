package gregtech.common.ec.multiblock;

import java.util.function.ToLongFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.ec.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IPowerSetupComponent;
import gregtech.api.logic.ProcessingLogic;
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

    @Override
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
    }
}
