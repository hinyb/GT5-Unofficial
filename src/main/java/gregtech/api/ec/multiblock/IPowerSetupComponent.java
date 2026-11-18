package gregtech.api.ec.multiblock;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;

public interface IPowerSetupComponent extends IProcessingLogicSetupComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IPowerSetupComponent> KEY = ComponentKey.unique();
}
