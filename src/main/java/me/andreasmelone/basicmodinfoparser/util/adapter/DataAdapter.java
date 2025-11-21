/*
 * MIT License
 *
 * Copyright (c) 2024-2025 RaydanOMGr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.andreasmelone.basicmodinfoparser.util.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * An abstract class for standardizing access to data from different sources,
 * such as JSON or TOML. This allows for a unified way of retrieving values,
 * regardless of the underlying data format.
 *
 * @param <T> The type of the underlying data source (e.g., JsonObject, TomlTable).
 * @param <A> The type of the array-like data structure in the source (e.g., JsonArray, TomlArray).
 */
public abstract class DataAdapter<T, A> {
    /**
     * The underlying data source object.
     */
    protected T backingObject;

    /**
     * Constructs a new {@code DataAdapter} with the given backing object.
     */
    public DataAdapter(T backingObject) {
        this.backingObject = backingObject;
    }

    /**
     * Retrieves a string value for a given key.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the string value, or empty if not found.
     */
    public abstract Optional<String> getString(String key);

    /**
     * Retrieves a boolean value for a given key.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the boolean value, or empty if not found.
     */
    public abstract Optional<Boolean> getBoolean(String key);

    /**
     * Retrieves an array-like data structure for a given key.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the array-like data, or empty if not found.
     */
    public abstract Optional<A> getArray(String key);

    /**
     * Checks if a value for the given key exists.
     *
     * @param key The key to check.
     * @return {@code true} if the key exists, otherwise {@code false}.
     */
    public abstract boolean hasKey(String key);

    /**
     * Returns the underlying data source object.
     *
     * @return The raw data source object.
     */
    public T getBackingObject() {
        return backingObject;
    }

    /**
     * Retrieves a list of strings from a key that can be either a single string or an array of strings.
     *
     * @param key The key to look up.
     * @return A {@link List} of strings, or an empty list if not found.
     */
    public List<String> getListOrString(String key) {
        return Collections.emptyList();
    }
}
