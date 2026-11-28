package gregtech.common.ec.multiblock;

import static gregtech.api.ec.IECMachine.ON_DO_SOUND;
import static gregtech.api.ec.IECMachine.ON_START_SOUND_LOOP;
import static gregtech.api.ec.component.IRecipeLogicComponent.ON_RECIPE_START;
import static gregtech.api.metatileentity.implementations.MTEMultiBlockBase.PROCESS_START_SOUND_INDEX;

import org.jetbrains.annotations.NotNull;

import gregtech.api.ec.IECMultiblock;
import gregtech.api.ec.component.IRecipeLogicComponent;
import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTUtility;
import gregtech.common.ec.multiblock.internal.BaseMultiblockComponent;

public class RecipeSoundComponent extends BaseMultiblockComponent {

    private final SoundResource processStartSound;
    private final int timeBetweenProcessSounds;

    public RecipeSoundComponent(SoundResource processStartSound, int timeBetweenProcessSounds) {
        this.processStartSound = processStartSound;
        this.timeBetweenProcessSounds = timeBetweenProcessSounds;
    }

    public void onRecipeStart(@NotNull IRecipeLogicComponent recipeLogicComponent) {
        getMachine().sendLoopStart(PROCESS_START_SOUND_INDEX);
    }

    public boolean playProcessStartSound(byte aIndex, double aX, double aY, double aZ) {
        if (aIndex == PROCESS_START_SOUND_INDEX) {
            GTUtility.doSoundAtClient(processStartSound, timeBetweenProcessSounds, 1.0F, aX, aY, aZ);
            return true;
        }
        return false;
    }

    @Override
    protected void onAdd(@NotNull IECMultiblock multiblock) {
        super.onAdd(multiblock);
        connect(ON_DO_SOUND, this::playProcessStartSound);
        connect(ON_START_SOUND_LOOP, this::playProcessStartSound);
        connect(ON_RECIPE_START, IRecipeLogicComponent.IOnRecipeStart.SOUND, this::onRecipeStart);
    }
}
