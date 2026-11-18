package gregtech.api.ec;

public interface ISlotUpdateAwareComponent extends IComponent {

    ComponentKey.ListKey<ISlotUpdateAwareComponent> KEY = ComponentKey.list();

    void updateSlots();
}
