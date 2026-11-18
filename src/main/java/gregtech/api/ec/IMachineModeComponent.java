package gregtech.api.ec;

import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Pos2d;

public interface IMachineModeComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IMachineModeComponent> KEY = ComponentKey.unique();

    int getMachineModeCount();

    int getMachineMode();

    void setMachineMode(int index);

    int nextMachineMode();

    @Nullable
    UITexture getMachineModeIcon(int index);

    @NotNull
    IMachineModeComponent addMachineModeIcons(UITexture... icons);

    @NotNull
    String getMachineModeKey(int index);

    default @NotNull String getMachineModeKey() {
        return getMachineModeKey(getMachineMode());
    }

    default @NotNull String getMachineModeName(int index) {
        return StatCollector.translateToLocal(getMachineModeKey(index));
    }

    default @NotNull String getMachineModeName() {
        return getMachineModeName(getMachineMode());
    }

    @NotNull
    Pos2d getMachineModeSwitchButtonPos();

    void onMachineModeSwitchClick();
}
