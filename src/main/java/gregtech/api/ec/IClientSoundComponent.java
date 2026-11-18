package gregtech.api.ec;

public interface IClientSoundComponent extends IComponent {

    ComponentKey.ListKey<IClientSoundComponent> KEY = ComponentKey.list();

    boolean onDoSound(byte aIndex, double aX, double aY, double aZ);

    boolean onStartSoundLoop(byte aIndex, double aX, double aY, double aZ);
}
