package gregtech.common.ec.multiblock;

import gregtech.api.ec.IEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IMultiblockEnergyInputLogicComponent;
import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.util.ExoticEnergyInputHelper;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class StandardPowerSetupComponent extends BaseMultiblockComponent implements IProcessingLogicSetupComponent {
    @Override
    public void setupProcessingLogic(ProcessingLogic logic) {
        boolean useSingleAmp = !energyInputLogic.isDebugEnergyPresent() && energyInputLogic.getNormalEnergyHatches().size() == 1 && energyInputLogic.getExoticEnergyHatches().isEmpty();
        long maxInputAmps = useSingleAmp ? 1 : ExoticEnergyInputHelper.getMaxWorkingInputAmpsMulti(energyInputLogic.getNormalEnergyHatches());
        logic.setAvailableVoltage(energyInputLogic.getAverageInputVoltage());
        logic.setAvailableAmperage(maxInputAmps);
        logic.setAmperageOC(true);
    }

    private IMultiblockEnergyInputLogicComponent energyInputLogic;
    @Override
    public void onComponentsReady()
    {
        energyInputLogic = getComponent(IMultiblockEnergyInputLogicComponent.class);
    }
}
