package gregtech.api.ec;

public interface IOnMachineRemovalComponent extends IComponent {

    ComponentKey.ListKey<IOnMachineRemovalComponent> KEY = ComponentKey.list();

    void onMachineRemoval();
}
