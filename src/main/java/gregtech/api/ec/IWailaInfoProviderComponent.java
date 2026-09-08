package gregtech.api.ec;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public interface IWailaInfoProviderComponent extends IComponent {
    void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
                      IWailaConfigHandler config);
    void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
                         int z);
}
