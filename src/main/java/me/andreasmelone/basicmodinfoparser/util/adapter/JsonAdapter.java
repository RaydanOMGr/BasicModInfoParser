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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link DataAdapter} implementation for {@link JsonObject}. This class
 * provides a standardized way to access data from a JSON structure, allowing
 * for consistent data retrieval regardless of the underlying format.
 */
public class JsonAdapter extends DataAdapter<JsonObject, JsonArray> {
    /**
     * Constructs a {@link JsonAdapter} with the given {@link JsonObject}.
     *
     * @param jsonObject The JSON object to adapt.
     */
    public JsonAdapter(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public Optional<String> getString(String key) {
        return getElement(key)
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsString);
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return getElement(key)
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsBoolean);
    }

    @Override
    public Optional<JsonArray> getArray(String key) {
        return getElement(key)
                .filter(JsonElement::isJsonArray)
                .map(JsonElement::getAsJsonArray);
    }

    @Override
    public <V> List<V> getList(String key, Function<DataAdapter<JsonObject, JsonArray>, V> mapper) {
        List<V> list = new ArrayList<>();
        getArray(key).ifPresent(array -> {
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    list.add(mapper.apply(new JsonAdapter(element.getAsJsonObject())));
                }
            }
        });
        return list;
    }

    @Override
    public boolean hasKey(String key) {
        return getElement(key).isPresent();
    }

    @Override
    public List<String> getListOrString(String key) {
        // When searching for author names, these are the situations we
        // need to take into account:
        //
        // ["example", "example2", ...]
        // (From Legacy Forge, Fabric and Quilt)
        //
        // [{ "name": "example" }, { "name": "example2" }, ...]
        // (Also from Fabric and Quilt)
        //
        // { "Name": "Role", "Name2": "Role2", ... } (Only in Quilt)
        //
        // The first two ways of representing author names are interchangeable in Fabric,
        // so an array could have both types at the same time!

        if (!hasKey(key)) return Collections.emptyList();

        JsonElement element = backingObject.get(key);
        List<String> result = new ArrayList<>();

        if (!element.isJsonArray() && !element.isJsonObject()) return Collections.emptyList();

        // Since both on Forge Legacy (https://docs.minecraftforge.net/en/1.13.x/gettingstarted/structuring/)
        // and Fabric (https://wiki.fabricmc.net/documentation:fabric_mod_json#metadata) the
        // author names are inside a json array, we can assume that, if the element is an object,
        // we are dealing with Quilt syntax
        if (element.isJsonObject()) {
            JsonObject asObject = element.getAsJsonObject();
            result.addAll(asObject.keySet());
            return result;
        }

        JsonArray asJsonArray = element.getAsJsonArray();
        for (JsonElement jsonElement : asJsonArray) {
            // Case number 1: "authors": ["example"]
            if (jsonElement.isJsonPrimitive()) {
                // Since we found an author name, we can add it to the list
                result.add(jsonElement.getAsString());
                continue;
            }

            // Case number 2: "authors": [{ "name": "example" }, {"..."}]
            if (jsonElement.isJsonObject()) {
                JsonObject asObject = asJsonArray.get(0).getAsJsonObject();
                if (asObject.has("name")) {
                    // Since we found an author name, we can add it to the list
                    result.add(asObject.get("name").getAsString());
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a {@link JsonElement} from the backing {@link JsonObject} based on the given key.
     * The key can be a dot-separated path to access nested elements.
     *
     * @param key The key or path to the element.
     * @return An {@link Optional} containing the {@link JsonElement} if found, otherwise empty.
     */
    private Optional<JsonElement> getElement(String key) {
        String[] parts = key.split("\\.");
        JsonElement current = backingObject;

        for (String part : parts) {
            if (current == null || !current.isJsonObject()) {
                return Optional.empty();
            }
            JsonObject obj = current.getAsJsonObject();
            if (!obj.has(part)) {
                return Optional.empty();
            }
            current = obj.get(part);
        }
        return Optional.ofNullable(current);
    }
}