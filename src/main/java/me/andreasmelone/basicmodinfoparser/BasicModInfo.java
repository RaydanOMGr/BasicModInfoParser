package me.andreasmelone.basicmodinfoparser;

import java.util.Objects;

public class BasicModInfo {
    private static final BasicModInfo EMPTY = new BasicModInfo(null, null, null, null);

    private final String id;
    private final String name;
    private final String version;
    private final String description;

    public BasicModInfo(String id, String name, String version, String description) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicModInfo that = (BasicModInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, description);
    }

    @Override
    public String toString() {
        return "BasicModInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public static BasicModInfo empty() {
        return EMPTY;
    }
}
