package gregtech.api.ec;

public interface IPollutionLogicComponent extends IUniqueComponent {

    ComponentKey.UniqueKey<IPollutionLogicComponent> KEY = ComponentKey.unique();

    int getAveragePollutionPercentage();

    boolean polluteEnvironment(int aPollutionLevel);
}
