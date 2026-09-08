package gregtech.api.ec;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IRunningTickableComponent extends IComponent {
    boolean onRunningTick(@Nullable ItemStack aStack);
}
