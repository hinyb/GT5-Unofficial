package gregtech.common.ec.multiblock.internal;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.multiblock.IHatchContainerComponent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;

public abstract class BaseHatchContainerComponent extends BaseMultiblockComponent implements IHatchContainerComponent {

    protected final void onHatchAttached(@NotNull IMetaTileEntity metaTile, int baseCasingIndex) {
        if (metaTile instanceof MTEHatch hatch) {
            hatch.updateTexture(baseCasingIndex);
            hatch.updateCraftingIcon(getMachine().getMachineCraftingIcon(), getMachine().getBaseMetaTileEntity());
        }
        for (var listener : listeners(ON_HATCH_ATTACHED)) {
            listener.onHatchAttached(metaTile, baseCasingIndex);
        }
    }

    protected final void clearCraftingIcons(@NotNull Iterable<?> hatches) {
        IGregTechTileEntity owner = getMachine().getBaseMetaTileEntity();
        for (var obj : hatches) {
            if (obj instanceof MTEHatch hatch) {
                hatch.updateCraftingIcon(null, owner);
            }
        }
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connectHatchLifecycle();
    }
}
