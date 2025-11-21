package me.andreasmelone.basicmodinfoparser.platform.dependency.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.StandardDependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.FabricVersionRange;
import me.andreasmelone.basicmodinfoparser.util.adapter.JsonAdapter;

import java.util.*;

public class QuiltDependencyParser implements IDependencyParser<JsonAdapter, Dependency> {

    public List<Dependency> parse(final String[] keys, final JsonAdapter modAdapter) {
        final List<Dependency> result = new ArrayList<>();

        for (final String key : keys) {
            if (!modAdapter.hasKey(key)) continue;

            final JsonElement element = modAdapter.getBackingObject().get(key);

            // In Quilt, all the dependency properties MUST be an array
            // (https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#the-depends-field)
            // (https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#the-breaks-field)
            if (!element.isJsonArray()) continue;

            final JsonArray asArray = element.getAsJsonArray();
            if (asArray.size() == 0) continue;

            // Get the dependencies present in the current key
            for (final JsonElement dependency : asArray) {
                result.addAll(parseSingleDependency(dependency));
            }
        }

        return result;
    }

    /**
     * Parses a single dependency json element.
     * According to
     * <a href="https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#dependency-objects">
     * Quilt's official specification for dependency objects</a>, a single Dependency can be of one of three types:
     * <ul>
     *     <li>An object containing at least the {@code id} field</li>
     *     <li>A string mod identifier in the form of either {@code mavenGroup:modId} or {@code modId}</li>
     *     <li>An array of dependency objects</li>
     * </ul>
     *
     * @param dependency The dependency JSON element. The type can be any of the aforementioned types.
     * @return A list of dependencies, or an empty optional if the element is not a valid dependency
     */
    private List<Dependency> parseSingleDependency(
            final JsonElement dependency
    ) {
        final List<Dependency> result = new ArrayList<>();

        // If the passed dependency's type is not an array,
        // add it to an array so the parsing logic is the same.
        final JsonArray jsonElements = dependency.isJsonArray()
                ? dependency.getAsJsonArray()
                : new JsonArray();

        if (jsonElements.size() == 0) jsonElements.add(dependency);


        for (final JsonElement dependencyElement : jsonElements) {
            // The supported versions for the current dependency
            List<String> versions = new ArrayList<>(Collections.singletonList("*"));

            // First case: string
            // A string mod identifier in the form of either "mavenGroup:modId" or "modId"
            if (dependencyElement.isJsonPrimitive() && dependencyElement.getAsJsonPrimitive().isString()) {
                final String asString = dependencyElement.getAsString();
                final String dependencyId = asString.contains(":") ? asString.split(":")[1] : asString;

                result.add(new StandardDependency<>(
                        dependencyId,
                        true,
                        FabricVersionRange.parse(versions.toArray(new String[0])).orElse(null)
                ));
                continue;
            }

            // Second case: object
            // An object containing at least the id field
            if (dependencyElement.isJsonObject()) {
                final JsonObject asObject = dependencyElement.getAsJsonObject();

                // The `id` field is required
                if (!asObject.has("id")) continue;

                final String dependencyId = asObject.get("id").getAsString();

                // The `Optional` field
                boolean mandatory = true;
                if (asObject.has("optional"))
                    mandatory = !asObject.getAsJsonPrimitive("optional").getAsBoolean();

                // The versions
                versions = Arrays.asList(getVersionsFor(asObject));

                result.add(new StandardDependency<>(
                        dependencyId,
                        mandatory,
                        FabricVersionRange.parse(
                                versions.toArray(new String[0])
                        ).orElse(null)
                ));
                continue;
            }

            // Third case: array
            // An array of dependency objects
            // Since it is a nested array with the same specifications as the previous
            // cases, we can use recursion to parse it.
            if (dependencyElement.isJsonArray()) result.addAll(parseSingleDependency(dependencyElement));
        }

        return result;
    }

    /**
     * Helper method to retrieve the {@code versions} array from a dependency.
     * This method takes into account the
     * <a href="https://github.com/QuiltMC/rfcs/blob/main/specification/0002-quilt.mod.json.md#the-versions-field">
     * Quilt official specification for the versions field.
     * </a> on GitHub.
     *
     * @param dependencyElement the {@link JsonAdapter} containing the dependency data
     * @return an array of versions supported by the dependency
     */
    private String[] getVersionsFor(JsonElement dependencyElement) {
        List<String> result = new ArrayList<>();

        // The "versions" field can be a string, an array of strings (deprecated) or an object

        // First case: object
        // In this case, the object must contain a single field, which must either be `any` or `all`
        // The field value must be an array, with more constraints. Each element of the array must
        // either be a string version specifier, or an object which is interpreted in the same way as
        // the versions field itself.
        if (dependencyElement.isJsonObject()) {
            final JsonObject asObject = dependencyElement.getAsJsonObject();

            if (asObject.has("any") || asObject.has("all")) {
                final JsonElement any = asObject.get("any");
                final JsonElement all = asObject.get("all");
                final JsonObject notNull;

                // Find which key type the object has (must be either `any` or `all`)
                // and use the non-null one to make a recursive call to getVersionsFor
                if (any != null) notNull = any.getAsJsonObject();
                else if (all != null) notNull = all.getAsJsonObject();
                else throw new RuntimeException("Dependency element must have 'any' or 'all': " + dependencyElement);

                List<String> embeddedVersions = Arrays.asList(getVersionsFor(notNull));
                // Embedded versions found, add them to the result list and return
                result.addAll(embeddedVersions);
                return result.toArray(new String[0]);
            }

            // The required `any` or `all` fields were not found, return an empty list
            return new String[0];
        }

        // Second and third cases: string, array of strings

        // As a string, the content should be a single version specifier defining the versions
        // this dependency applies to.

        // As an array of strings, regardless of being deprecated, it should be
        // an array of version specifiers defining the versions this dependency applies to.

        // Since there's no fundamental difference in the way a single version without an array
        // and a version element inside an array are written, we can treat a single version string
        // as a version array with only one element.

        JsonArray versionsArray = dependencyElement.isJsonArray()
                ? dependencyElement.getAsJsonArray()
                : new JsonArray();

        if (versionsArray.size() == 0)
            versionsArray.add(dependencyElement); // In this case, the version element turned out to be a single string

        // In the array, the only allowed type is string, so we don't have to worry about recursion here.
        for (JsonElement versionElement : versionsArray) {
            if (!versionElement.isJsonPrimitive() || !versionElement.getAsJsonPrimitive().isString()) continue;

            result.add(versionElement.getAsString());
        }

        return result.toArray(new String[0]);
    }
}
