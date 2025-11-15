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

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link DataAdapter} implementation for {@link TomlTable}. This class
 * provides a standardized way to access data from a TOML structure, allowing
 * for consistent data retrieval regardless of the underlying format.
 */
public class TomlAdapter implements DataAdapter<TomlTable, TomlArray> {
    private final TomlTable tomlTable;

    /**
     * Constructs a {@link TomlAdapter} with the given {@link TomlTable}.
     *
     * @param tomlTable The TOML table to adapt.
     */
    public TomlAdapter(TomlTable tomlTable) {
        this.tomlTable = tomlTable;
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.ofNullable(tomlTable.getString(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable(tomlTable.getBoolean(key));
    }

    @Override
    public Optional<TomlArray> getArray(String key) {
        return Optional.ofNullable(tomlTable.getArray(key));
    }

    @Override
    public <V> List<V> getList(String key, Function<DataAdapter<TomlTable, TomlArray>, V> mapper) {
        List<V> list = new ArrayList<>();
        getArray(key).ifPresent(array -> {
            for (int i = 0; i < array.size(); i++) {
                TomlTable table = array.getTable(i);
                if (table != null) {
                    list.add(mapper.apply(new TomlAdapter(table)));
                }
            }
        });
        return list;
    }

    @Override
    public boolean has(String key) {
        return tomlTable.contains(key);
    }

    @Override
    public TomlTable getBackingObject() {
        return tomlTable;
    }

    @Override
    public List<String> getListOrString(String key) {
        if (!has(key)) {
            return Collections.emptyList();
        }

        Optional<TomlArray> arrayOptional = getArray(key);
        return arrayOptional.map(tomlArray -> tomlArray.toList().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .orElseGet(() -> getString(key)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList()));

    }
}
