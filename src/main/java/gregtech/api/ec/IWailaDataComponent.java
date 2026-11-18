package gregtech.api.ec;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IWailaDataComponent extends IComponent {

    ComponentKey.ListKey<IWailaDataComponent> KEY = ComponentKey.list();

    void appendWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z);
}
