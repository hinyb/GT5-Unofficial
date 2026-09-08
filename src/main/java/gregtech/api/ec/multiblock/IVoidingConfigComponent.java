package gregtech.api.ec.multiblock;

import gregtech.api.ec.IComponent;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.enums.VoidingMode;

import java.util.Set;

public interface IVoidingConfigComponent extends IMultiblockComponent, IUniqueComponent {
    default Set<VoidingMode> getAllowedVoidingModes() {
        return VoidingMode.ALL_OPTIONS;
    }

    VoidingMode getVoidingMode();

    void setVoidingMode(VoidingMode mode);

    /**
     * @return if this machine is configured to not void excess item.
     */
    default boolean protectsExcessItem() {
        return getVoidingMode().protectItem;
    }

    /**
     * @return if this machine is configured to not void excess fluid.
     */
    default boolean protectsExcessFluid() {
        return getVoidingMode().protectFluid;
    }

}
