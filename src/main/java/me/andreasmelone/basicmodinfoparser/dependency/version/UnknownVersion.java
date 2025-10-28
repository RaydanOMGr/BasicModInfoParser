package me.andreasmelone.basicmodinfoparser.dependency.version;

import org.jetbrains.annotations.NotNull;

public class UnknownVersion implements Version<UnknownVersion> {
    private final String version;
    public UnknownVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "UnknownVersion{" +
                "version='" + version + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NotNull UnknownVersion other) {
        return 0;
    }

    public static UnknownVersion parse(String ver) {
        return new UnknownVersion(ver);
    }
}
