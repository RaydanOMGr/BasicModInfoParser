package me.andreasmelone.basicmodinfoparser.dependency.version;

public interface VersionRange<T extends Version<T>> {
    String getStringRepresentation();

    boolean contains(T version);
}
