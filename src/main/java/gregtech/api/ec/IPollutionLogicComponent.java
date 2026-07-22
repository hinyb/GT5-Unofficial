package gregtech.api.ec;

import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.api.metatileentity.implementations.MTEHatchMuffler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IPollutionLogicComponent extends IComponent {
    int getAveragePollutionPercentage();
    boolean polluteEnvironment(int aPollutionLevel);
}
