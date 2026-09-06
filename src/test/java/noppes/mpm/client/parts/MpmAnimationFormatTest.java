package noppes.mpm.client.parts;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MpmAnimationFormatTest {
    @Test void bundledLibrariesStillParse() throws Exception {
        try (var files = java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/assets"))) {
            var libraries = files.filter(path -> path.toString().contains("/animations/")
                    && path.toString().endsWith(".json")).toList();
            assertFalse(libraries.isEmpty());
            for (var path : libraries) {
                try (var reader = java.nio.file.Files.newBufferedReader(path)) {
                    var document = JsonParser.parseReader(reader);
                    assertTrue(MpmAnimationFormat.isMpm(document), path.toString());
                    assertFalse(MpmPart.loadAnimations(document.getAsJsonObject()).isEmpty(), path.toString());
                }
            }
        }
    }

    @Test void ignoresForeignAnimationEnvelopes() {
        for (String json : new String[]{"[]", "{\"format_version\":\"1.8.0\",\"animations\":{}}",
                "{\"animations\":{\"animation.iron_cage.idle\":{}}}", "{\"unrelated\":{}}"}) {
            assertFalse(MpmAnimationFormat.isMpm(JsonParser.parseString(json)), json);
        }
    }

    @Test void acceptsLegacyFormatRegardlessOfNamespaceAndPreservesValidation() {
        assertTrue(MpmAnimationFormat.isMpm(JsonParser.parseString("{\"IDLE\":{}}")));
        // A recognized but malformed entry must reach the strict parser, not disappear silently.
        assertTrue(MpmAnimationFormat.isMpm(JsonParser.parseString("{\"idle\":null,\"typo\":{}}")));
        assertThrows(RuntimeException.class, () -> MpmPart.loadAnimations(JsonParser.parseString("{\"idle\":{}}").getAsJsonObject()));
    }
}
