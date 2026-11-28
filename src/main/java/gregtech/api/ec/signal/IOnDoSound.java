package gregtech.api.ec.signal;

@FunctionalInterface
public interface IOnDoSound {

    boolean onDoSound(byte aIndex, double aX, double aY, double aZ);

    int FALLBACK = 1000;
}
