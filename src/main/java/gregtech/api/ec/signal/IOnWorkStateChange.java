package gregtech.api.ec.signal;

@FunctionalInterface
public interface IOnWorkStateChange {

    void onWorkStateChanged(boolean enabled);
}
