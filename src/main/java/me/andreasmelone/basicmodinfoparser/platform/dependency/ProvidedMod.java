package me.andreasmelone.basicmodinfoparser.platform.dependency;

import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;

import java.util.Objects;

public class ProvidedMod<T extends Version<T>> {
    private final String id;
    private final Version<T> version;

    public ProvidedMod(String id, Version<T> version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public Version<T> getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ProvidedMod{" +
                "id='" + id + '\'' +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProvidedMod<?> that = (ProvidedMod<?>) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }
}
