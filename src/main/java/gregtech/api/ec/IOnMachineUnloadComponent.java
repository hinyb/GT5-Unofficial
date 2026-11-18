package gregtech.api.ec;

public interface IOnMachineUnloadComponent extends IComponent {

    ComponentKey.ListKey<IOnMachineUnloadComponent> KEY = ComponentKey.list();

    void onMachineUnload();
}
