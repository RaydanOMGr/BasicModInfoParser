package me.andreasmelone.basicmodinfoparser.platform.dependency.version;

import java.util.Optional;

public interface Version<T extends Version<T>> extends Comparable<T> {
    @SuppressWarnings("unchecked")
    default Optional<T> optional() {
        return Optional.of((T)this);
    }

    /**
     * @return this version as a human-readable string
     */
    String getStringRepresentation();

    /**
     * @return the type of {@link Version}
     */
    Class<T> getType();
}
