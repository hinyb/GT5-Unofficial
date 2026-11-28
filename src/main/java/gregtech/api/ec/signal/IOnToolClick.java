package gregtech.api.ec.signal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IOnToolClick {

    boolean onToolRightClick(ForgeDirection side, @Nullable ForgeDirection wrenchingSide, EntityPlayer player, float x,
        float y, float z, ItemStack tool);

    int FALLBACK = 1000;

    final class ScrewdriverPriority {

        public static final int INPUT_SEPARATION = -2000;
        public static final int RECIPE_LOCKING = -1000;
    }
}
