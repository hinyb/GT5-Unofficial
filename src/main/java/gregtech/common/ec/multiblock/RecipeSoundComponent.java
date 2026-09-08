package gregtech.common.ec.multiblock;

import gregtech.api.ec.IClientSoundComponent;
import gregtech.api.ec.IOnRecipeStartComponent;
import gregtech.api.ec.IRecipeLogicComponent;
import gregtech.api.ec.IUniqueComponent;
import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.metatileentity.implementations.MTEMultiBlockBase.INTERRUPT_SOUND_INDEX;
import static gregtech.api.metatileentity.implementations.MTEMultiBlockBase.PROCESS_START_SOUND_INDEX;

public class RecipeSoundComponent extends BaseMultiblockComponent implements IUniqueComponent, IOnRecipeStartComponent, IClientSoundComponent {
    private final SoundResource processStartSound;
    private final int timeBetweenProcessSounds;

    public RecipeSoundComponent(SoundResource processStartSound, int timeBetweenProcessSounds) {
        this.processStartSound = processStartSound;
        this.timeBetweenProcessSounds = timeBetweenProcessSounds;
    }

    @Override
    public void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        getMachine().sendLoopStart(PROCESS_START_SOUND_INDEX);
    }

    @Override
    public @NotNull RecipeStartPriority getRecipeStartPriority() {
        return RecipeStartPriority.SOUND;
    }

    @Override
    public boolean onDoSound(byte aIndex, double aX, double aY, double aZ) {
        switch (aIndex){
            case PROCESS_START_SOUND_INDEX -> {
                GTUtility.doSoundAtClient(processStartSound, timeBetweenProcessSounds, 1.0F, aX, aY, aZ);
                return true;
            }
            case INTERRUPT_SOUND_INDEX -> {
                GTUtility
                    .doSoundAtClient(SoundResource.IC2_MACHINES_INTERRUPT_ONE, 100, 1.0F, aX, aY, aZ);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onStartSoundLoop(byte aIndex, double aX, double aY, double aZ) {
        if (aIndex == PROCESS_START_SOUND_INDEX ){
            GTUtility.doSoundAtClient(processStartSound, timeBetweenProcessSounds, 1.0F, aX, aY, aZ);
            return true;
        }
        return false;
    }
}
