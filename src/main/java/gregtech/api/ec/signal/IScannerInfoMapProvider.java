package gregtech.api.ec.signal;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IScannerInfoMapProvider {

    void appendInfoMap(@NotNull Map<String, String> map);
}
