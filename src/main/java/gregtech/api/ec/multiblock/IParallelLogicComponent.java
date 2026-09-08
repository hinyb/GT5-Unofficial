package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;

public interface IParallelLogicComponent extends IUniqueComponent, IMultiblockComponent {
    boolean isAlwaysMaxParallel();
    void setAlwaysMaxParallel(boolean alwaysMaxParallel);
    int getPowerPanelMaxParallel();
    void setPowerPanelMaxParallel(int maxParallel);
    int getMaxParallelRecipes();
    default int getTrueParallel() {
        return Math.max(1, isAlwaysMaxParallel() ? getMaxParallelRecipes() : Math.min(getMaxParallelRecipes(), getPowerPanelMaxParallel()));
    }
}
