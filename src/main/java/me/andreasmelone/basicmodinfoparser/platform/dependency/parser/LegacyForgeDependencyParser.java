package me.andreasmelone.basicmodinfoparser.platform.dependency.parser;

import com.google.gson.JsonElement;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.DependencySide;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.ForgeDependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersionRange;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.Ordering;
import me.andreasmelone.basicmodinfoparser.util.adapter.JsonAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LegacyForgeDependencyParser implements IDependencyParser<JsonAdapter, ForgeDependency> {
    @Override
    public List<ForgeDependency> parse(String[] keys, JsonAdapter modAdapter) {
        if (modAdapter == null || keys.length == 0) return Collections.emptyList();

        List<ForgeDependency> dependencies = new ArrayList<>();

        for (String dependencyKey : keys) {
            modAdapter.getArray(dependencyKey).ifPresent(dependenciesArray -> {
                for (JsonElement element : dependenciesArray) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        parseLegacyForgeDependency(element.getAsString()).ifPresent(dependencies::add);
                    }
                }
            });
        }

        return dependencies;
    }

    private Optional<ForgeDependency> parseLegacyForgeDependency(String dependencyString) {
        String modId;
        String version = null;
        Ordering ordering = Ordering.NONE;

        String[] splitPrefix = dependencyString.split(":", 2);
        if (splitPrefix.length > 1) {
            ordering = Ordering.getFromString(splitPrefix[0]);
            dependencyString = splitPrefix[1];
        }

        String[] splitVersion = dependencyString.split("@");
        if (splitVersion.length > 1) {
            version = splitVersion[1];
            dependencyString = splitVersion[0];
        }

        modId = dependencyString;

        if (modId == null || modId.isEmpty()) {
            return Optional.empty();
        }

        Optional<MavenVersionRange> range = MavenVersionRange.parse(version);
        return Optional.of(new ForgeDependency(modId, range.orElse(null), true, ordering, DependencySide.BOTH));

    }
}
