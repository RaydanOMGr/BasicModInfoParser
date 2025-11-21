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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link DataAdapter} implementation for {@link TomlTable}. This class
 * provides a standardized way to access data from a TOML structure, allowing
 * for consistent data retrieval regardless of the underlying format.
 */
public class TomlAdapter extends DataAdapter<TomlTable, TomlArray> {
    public TomlAdapter(TomlTable backingObject) {
        super(backingObject);
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.ofNullable(backingObject.getString(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable(backingObject.getBoolean(key));
    }

    @Override
    public Optional<TomlArray> getArray(String key) {
        return Optional.ofNullable(backingObject.getArray(key));
    }

    @Override
    public boolean hasKey(String key) {
        return backingObject.contains(key);
    }

    @Override
    public List<String> getListOrString(String key) {
        if (!hasKey(key)) {
            return Collections.emptyList();
        }

        if (backingObject.isString(key)) {
            return Collections.singletonList(backingObject.getString(key));
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
