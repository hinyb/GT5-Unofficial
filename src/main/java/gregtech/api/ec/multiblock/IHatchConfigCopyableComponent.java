package gregtech.api.ec.multiblock;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.ComponentKey;
import gregtech.api.metatileentity.implementations.MTEHatch;

public interface IHatchConfigCopyableComponent extends IHatchContainerComponent {

    ComponentKey.ListKey<IHatchConfigCopyableComponent> KEY = ComponentKey.list();

    @NotNull
    Map<String, @NotNull Collection<? extends MTEHatch>> getCopyableHatches();
}
