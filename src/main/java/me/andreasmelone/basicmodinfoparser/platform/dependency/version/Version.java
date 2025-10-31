package me.andreasmelone.basicmodinfoparser.platform.dependency.version;

import java.util.Optional;

public interface Version<T extends Version<T>> extends Comparable<T> {
    @SuppressWarnings("unchecked")
    default Optional<T> optional() {
        return Optional.of((T)this);
    }

    /**
     * @return the type of {@link Version}
     */
    Class<T> getType();
}
