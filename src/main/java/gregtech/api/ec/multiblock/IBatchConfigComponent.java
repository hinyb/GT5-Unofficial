package gregtech.api.ec.multiblock;

import gregtech.api.ec.ComponentKey;
import gregtech.api.ec.IUniqueComponent;

public interface IBatchConfigComponent extends IMultiblockComponent, IUniqueComponent {

    ComponentKey.UniqueKey<IBatchConfigComponent> KEY = ComponentKey.unique();

    boolean isBatchModeEnabled();

    IBatchConfigComponent setBatchMode(boolean enabled);

    int getMaxBatchSize();

    IBatchConfigComponent setMaxBatchSize(int maxBatchSize);
}
