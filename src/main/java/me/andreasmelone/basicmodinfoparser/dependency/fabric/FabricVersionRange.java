package me.andreasmelone.basicmodinfoparser.dependency.fabric;

import me.andreasmelone.basicmodinfoparser.dependency.version.SemanticVersion;
import me.andreasmelone.basicmodinfoparser.dependency.version.VersionRange;

import java.util.Optional;

// TODO implement
public class FabricVersionRange implements VersionRange<SemanticVersion> {
    @Override
    public String getStringRepresentation() {
        return "";
    }

    @Override
    public boolean contains(SemanticVersion version) {
        return false;
    }

    public static Optional<FabricVersionRange> parse(String version) {
        return Optional.empty();
    }
}
