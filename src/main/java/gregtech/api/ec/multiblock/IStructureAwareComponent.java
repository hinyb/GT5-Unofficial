package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;

public interface IStructureAwareComponent extends IComponent {
    void onMachineValid();
    void onMachineInvalid();
    void onMachineCheckStart();
}
