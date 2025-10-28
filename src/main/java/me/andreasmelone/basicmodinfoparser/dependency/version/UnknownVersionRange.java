package me.andreasmelone.basicmodinfoparser.dependency.version;

public class UnknownVersionRange<T extends Version<T>> implements VersionRange<T> {
    private final String versionRange;

    public UnknownVersionRange(String versionRange) {
        this.versionRange = versionRange;
    }

    @Override
    public String getStringRepresentation() {
        return versionRange;
    }

    @Override
    public String toString() {
        return "UnknownVersionRange{" +
                "versionRange='" + versionRange + '\'' +
                '}';
    }

    @Override
    public boolean contains(T version) {
        return true;
    }

    public static UnknownVersionRange<?> parse(String version) {
        return new UnknownVersionRange<>(version);
    }
}
