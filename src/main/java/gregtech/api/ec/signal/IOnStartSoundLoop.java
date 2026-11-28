package gregtech.api.ec.signal;

@FunctionalInterface
public interface IOnStartSoundLoop {

    void onStartSoundLoop(byte aIndex, double aX, double aY, double aZ);
}
