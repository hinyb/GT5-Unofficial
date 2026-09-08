package gregtech.common.ec.multiblock;

import gregtech.api.ec.multiblock.IProcessingLogicSetupComponent;
import gregtech.api.ec.multiblock.IVoidingConfigComponent;
import gregtech.api.enums.VoidingMode;
import gregtech.api.logic.ProcessingLogic;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class VoidingConfigComponent extends BaseMultiblockComponent implements IVoidingConfigComponent, IProcessingLogicSetupComponent {
    private VoidingMode voidingMode = VoidingMode.VOID_ALL;

    @Override
    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    @Override
    public void setVoidingMode(VoidingMode mode) {
        voidingMode = mode;
    }

    @Override
    public void setupProcessingLogic(ProcessingLogic logic) {
        logic.setVoidProtection(protectsExcessItem(), protectsExcessFluid());
    }
}
