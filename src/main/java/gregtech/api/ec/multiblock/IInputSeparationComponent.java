package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;

public interface IInputSeparationComponent extends IMultiblockComponent, IUniqueComponent {
    boolean isInputSeparationEnabled();
    IInputSeparationComponent setInputSeparation(boolean enabled);
}
