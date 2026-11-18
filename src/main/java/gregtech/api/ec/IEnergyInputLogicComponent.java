package gregtech.api.ec;

public interface IEnergyInputLogicComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IEnergyInputLogicComponent> KEY = ComponentKey.unique();

    long getMaxInputVoltage();

    long getAverageInputVoltage();

    long getMaxInputAmps();

    long getMaxInputEu();

    long getMaxInputPower();

    long getInputVoltageTier();

    boolean drainEnergyInput(long aEU);
}
