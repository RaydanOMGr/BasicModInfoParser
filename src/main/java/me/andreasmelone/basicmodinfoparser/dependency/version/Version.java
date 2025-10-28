package me.andreasmelone.basicmodinfoparser.dependency.version;

import java.util.Optional;

public interface Version<T extends Version<T>> extends Comparable<T> {
    @SuppressWarnings("unchecked")
    default Optional<T> optional() {
        return Optional.of((T)this);
    }
}
