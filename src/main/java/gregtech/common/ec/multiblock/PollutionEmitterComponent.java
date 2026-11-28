package gregtech.common.ec.multiblock;

import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_TICK;
import static gregtech.api.ec.component.multiblock.IStructureLogicComponent.ON_STRUCTURE_VERIFY;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IPollutionEmitterComponent;
import gregtech.api.ec.component.IPollutionLogicComponent;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.ec.component.multiblock.IStructureLogicComponent;
import gregtech.api.enums.ECHatchElement;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.ErrorType;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class PollutionEmitterComponent extends BaseMultiblockComponent implements IPollutionEmitterComponent {

    public void checkMachine(@NotNull IGregTechTileEntity aBaseMetaTileEntity, @NotNull List<StructureError> errors) {
        int count = (int) ECHatchElement.Muffler.count(getMachine());
        if (count < 1) {
            errors.add(StructureErrors.hatchCount(ErrorType.TOO_FEW, ECHatchElement.Muffler, count, 1));
        }
    }

    @FunctionalInterface
    public interface IPollutionCalculator {

        /**
         * Gets the pollution produced per second by this multiblock, default to 0. Override this with its actual value
         * in
         * the code of the multiblock.
         *
         * This returns the unmodified raw pollution value, not the one after muffler discounts.
         *
         * @param aStack what is in controller
         */
        @Range(from = 0, to = Integer.MAX_VALUE)
        int getPollutionPerSecond(@Nullable ItemStack aStack);
    }

    private final @NotNull IPollutionCalculator pollutionCalculator;

    public PollutionEmitterComponent(@NotNull IPollutionCalculator pollutionCalculator) {
        this.pollutionCalculator = pollutionCalculator;
    }

    public int getPollutionPerSecond(ItemStack aStack) {
        return pollutionCalculator.getPollutionPerSecond(aStack);
    }

    public boolean onRecipeTick(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        if (!pollutionLogic.polluteEnvironment(getPollutionPerTick(getMachine().getControllerSlot()))) {
            stopMachine(ShutDownReasonRegistry.POLLUTION_FAIL);
            return false;
        }
        return true;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_RECIPE_TICK, IRecipeLogicComponent.IOnRecipeTick.POLLUTE, this::onRecipeTick);
        connect(ON_STRUCTURE_VERIFY, IStructureLogicComponent.IStructureCheckHandler.POLLUTION, this::checkMachine);
    }

    private IPollutionLogicComponent pollutionLogic;

    @Override
    public void onComponentsReady() {
        pollutionLogic = getComponent(IPollutionLogicComponent.KEY);
    }
}
