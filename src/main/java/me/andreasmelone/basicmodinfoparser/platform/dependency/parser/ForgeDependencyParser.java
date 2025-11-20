package me.andreasmelone.basicmodinfoparser.platform.dependency.parser;

import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.DependencySide;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.ForgeDependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersionRange;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.Ordering;
import me.andreasmelone.basicmodinfoparser.util.adapter.DataAdapter;
import me.andreasmelone.basicmodinfoparser.util.adapter.TomlAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ForgeDependencyParser implements IDependencyParser<TomlAdapter, ForgeDependency> {
    @Override
    public List<ForgeDependency> parse(String[] keys, TomlAdapter modInfo) {
        if (modInfo == null || keys.length == 0) return Collections.emptyList();

        List<ForgeDependency> dependencies = new ArrayList<>();

        for (String dependencyKey : keys) {
            modInfo.getArray(dependencyKey).ifPresent(dependenciesArray -> {
                for (int i = 0; i < dependenciesArray.size(); i++) {
                    TomlAdapter dependencyAdapter = new TomlAdapter(dependenciesArray.getTable(i));
                    dependencies.add(parseForgeDependency(dependencyAdapter));
                }
            });
        }
        return dependencies;
    }

    /**
     * Parses a {@link DataAdapter} into a {@link ForgeDependency} object.
     * <p>
     * This method extracts the necessary fields from a DataAdapter and
     * returns a corresponding {@link ForgeDependency} object.
     * </p>
     *
     * @param dependencyAdapter The DataAdapter containing the dependency's data.
     * @return A {@link ForgeDependency} object constructed from the values in the given DataAdapter.
     */
    @NotNull
    private static <T, A> ForgeDependency parseForgeDependency(TomlAdapter dependencyAdapter) {
        String depModId = dependencyAdapter.getString("modId").orElse("");
        boolean mandatory = dependencyAdapter.getBoolean("mandatory").orElse(true);
        String versionRange = dependencyAdapter.getString("versionRange").orElse(null);
        String ordering = dependencyAdapter.getString("ordering").orElse("NONE");
        String side = dependencyAdapter.getString("side").orElse("BOTH");

        Optional<MavenVersionRange> range = MavenVersionRange.parse(versionRange);
        return new ForgeDependency(
                depModId, range.orElse(null), mandatory,
                Ordering.getFromString(ordering), DependencySide.getFromString(side)
        );
    }
}
