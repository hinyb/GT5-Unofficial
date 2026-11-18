package gregtech.api.ec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IOnScrewdriverClickComponent extends IComponent {

    ComponentKey.ListKey<IOnScrewdriverClickComponent> KEY = ComponentKey.list();

    boolean onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool);
}
