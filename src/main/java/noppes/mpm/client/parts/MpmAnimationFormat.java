package noppes.mpm.client.parts;

import com.google.gson.JsonElement;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import noppes.mpm.constants.EnumAnimation;

/** Legacy MPM packs may use any namespace; directory names alone do not identify a format. */
public final class MpmAnimationFormat {
    private static final Set<String> NAMES = Arrays.stream(EnumAnimation.values())
            .map(value -> value.name().toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());

    private MpmAnimationFormat() {}

    public static boolean isMpm(JsonElement document) {
        if (!document.isJsonObject()) return false;
        var root = document.getAsJsonObject();
        // Bedrock/GeckoLib use an envelope, while MPM uses animation enum keys directly.
        if (root.has("format_version") || root.has("animations")) return false;
        // Recognize even malformed MPM entries, so the strict parser still reports their errors.
        return root.keySet().stream().anyMatch(key -> NAMES.contains(key.toLowerCase(Locale.ROOT)));
    }
}
