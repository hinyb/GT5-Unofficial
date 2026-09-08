package gregtech.api.ec.multiblock;

import gregtech.api.logic.ProcessingLogic;

public interface IProcessingLogicSetupComponent extends IMultiblockComponent {
    void setupProcessingLogic(ProcessingLogic logic);
}
