package gregtech.api.ec;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public interface IScannerInfoComponent extends IComponent {

    ComponentKey.ListKey<IScannerInfoComponent> KEY = ComponentKey
        .list(Comparator.comparingInt(IScannerInfoComponent::getScannerInfoPriority));

    enum ScannerInfoPriority {

        RECIPE(1000),
        ENERGY_INPUT(2000),
        ENERGY_CONSUME(3000),
        MAINTAIN(4000),
        EFFICIENCY(5000),
        POLLUTION(6000),
        RUN_TIME(7000);

        public final int priority;

        ScannerInfoPriority(int priority) {
            this.priority = priority;
        }
    }

    void appendInfoData(@NotNull List<String> data);

    void appendInfoMap(@NotNull Map<String, String> map);

    int getScannerInfoPriority();
}
