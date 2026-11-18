package gregtech.api.ec;

import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public interface IWailaBodyProviderComponent extends IComponent {

    ComponentKey.ListKey<IWailaBodyProviderComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IWailaBodyProviderComponent::getWailaBodyPriority));

    enum WailaBodyPriority {

        CUSTOM(0);

        public final int priority;

        WailaBodyPriority(int priority) {
            this.priority = priority;
        }
    }

    void appendWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config);

    int getWailaBodyPriority();
}
