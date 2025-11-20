package me.andreasmelone.basicmodinfoparser.platform.dependency.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.StandardDependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.FabricVersionRange;
import me.andreasmelone.basicmodinfoparser.util.adapter.JsonAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class FabricDependencyParser implements IDependencyParser<JsonAdapter, Dependency> {
    @Override
    public List<Dependency> parse(String[] keys, JsonAdapter modAdapter) {
        final List<Dependency> result = new ArrayList<>();

        for (String key : keys) {
            if (!modAdapter.hasKey(key) || !modAdapter.getBackingObject().get(key).isJsonObject()) {
                continue;
            }

            parseSingleDependency(key, modAdapter).ifPresent(result::add);
        }

        return result;
    }

    protected Optional<Dependency> parseSingleDependency(String key, JsonAdapter modAdapter) {
        // As the fabric documentation specifies, the dependency keys are specified in the format
        // of a string -> VersionRange dictionary, where the string key matches the desired ID.
        // (https://wiki.fabricmc.net/documentation:fabric_mod_json_spec#optional_fields_dependency_resolution)

        if (!modAdapter.hasKey(key) || !modAdapter.getBackingObject().get(key).isJsonObject()) return Optional.empty();

        final JsonObject dependencyObject = modAdapter.getBackingObject().getAsJsonObject(key);
        final boolean required = isRequired(key);

        // Extract all the keys from the found object
        for (Map.Entry<String, JsonElement> entry : dependencyObject.entrySet()) {
            final String dependencyId = entry.getKey();
            final JsonObject versionRange = entry.getValue().getAsJsonObject();

            // VersionRange can be a string or an array of strings

            // Case 1: String
            if (versionRange.isJsonPrimitive() && versionRange.getAsJsonPrimitive().isString()) {
                Optional<FabricVersionRange> range = FabricVersionRange.parse(versionRange.getAsString());
                if (range.isPresent()) return Optional.of(
                        new StandardDependency<>(dependencyId, required, range.get())
                );
            }

            // Case 2: array of Strings
            if (versionRange.isJsonArray()) {
                final String[] onlyStrings = StreamSupport.stream(
                                versionRange.getAsJsonArray().spliterator(),
                                false
                        ).filter(element ->
                                element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                        ).map(JsonElement::getAsString)
                        .toArray(String[]::new);

                Optional<FabricVersionRange> fabricVersionRange = FabricVersionRange.parse(onlyStrings);
                return fabricVersionRange.map(
                        range -> new StandardDependency<>(
                                dependencyId,
                                required,
                                range
                        )
                );
            }
        }

        // No parsing was successful, return an empty optional
        return Optional.empty();
    }

    protected boolean isRequired(String keyName) {
        switch (keyName) {
            case "depends":
            case "breaks":
                return true;
            default:
                return false;
        }
    }
}
