package gregtech.api.ec.multiblock;

import gregtech.api.ec.IUniqueComponent;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.jetbrains.annotations.NotNull;

public interface ICoilLogicComponent extends IUniqueComponent, IMultiblockComponent {
    void addCoilCoordinate(long packedCorrdinate);
    @NotNull LongArrayList getCoilCoordinates();
}
