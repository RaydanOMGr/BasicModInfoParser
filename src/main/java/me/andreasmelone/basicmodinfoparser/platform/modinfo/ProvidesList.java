package me.andreasmelone.basicmodinfoparser.platform.modinfo;

import me.andreasmelone.basicmodinfoparser.platform.dependency.ProvidedMod;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;

import java.util.List;

public interface ProvidesList<T extends Version<T>> {
    /**
     * @return the provided mod IDs
     * @see ProvidedMod
     */
    List<ProvidedMod<T>> getProvidedIds();

    /**
     * @return the type of the provided {@link Version} objects
     */
    Class<T> getType();
}
