package gregtech.api.ec;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IActivitySoundLoopComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IActivitySoundLoopComponent> KEY = ComponentKey.unique();

    /// Cleans up old sound loops and starts new ones if needed. Called once per tick.
    @SideOnly(Side.CLIENT)
    void startActivitySound();

    @SideOnly(Side.CLIENT)
    void restartActivitySound();
}
