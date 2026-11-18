package gregtech.api.ec.multiblock;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;

public interface IInputSeparationComponent extends IMultiblockComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IInputSeparationComponent> KEY = ComponentKey.unique();

    boolean isInputSeparationEnabled();

    IInputSeparationComponent setInputSeparation(boolean enabled);
}
