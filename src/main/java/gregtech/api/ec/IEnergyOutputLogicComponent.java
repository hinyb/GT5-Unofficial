package gregtech.api.ec;

public interface IEnergyOutputLogicComponent extends IComponent {
    default boolean addEnergyOutput(long aEU) {
        return addEnergyOutputMultipleDynamos(aEU, true);
    }
    boolean addEnergyOutputMultipleDynamos(long aEU, boolean aAllowMixedVoltageDynamos);
}
