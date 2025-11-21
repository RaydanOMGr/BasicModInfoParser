package me.andreasmelone.basicmodinfoparser.platform.dependency.parser;

import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.util.adapter.DataAdapter;

import java.util.List;

/**
 * An interface for parsing dependencies from a mod adapter.
 * This is a functional interface whose only method is {@link #parse(String[], DataAdapter)}.+
 *
 * @param <T> The type of the mod adapter
 * @param <D> The type of the dependency
 */
@FunctionalInterface
public interface IDependencyParser<T extends DataAdapter<?, ?>, D extends Dependency> {
    List<D> parse(String[] keys, T modAdapter);
}