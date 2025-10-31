package me.andreasmelone.basicmodinfoparser.platform.dependency;

import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;

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
}
