package gregtech.api.ec;

public interface IOnWorkStateChangeComponent extends IComponent {

    ComponentKey.ListKey<IOnWorkStateChangeComponent> KEY = ComponentKey.list();

    default void onEnableWorking() {}

    default void onDisableWorking() {}
}
