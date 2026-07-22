package gregtech.api.ec;

import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IEnergyInputLogicComponent extends IComponent {
    long getMaxInputVoltage();
    long getAverageInputVoltage();
    long getMaxInputAmps();
    long getMaxInputEu();
    long getMaxInputPower();
    long getInputVoltageTier();
    boolean drainEnergyInput(long aEU);
}
