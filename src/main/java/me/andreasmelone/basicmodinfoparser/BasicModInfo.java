/*
 * MIT License
 *
 * Copyright (c) 2024 RaydanOMGr
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
package me.andreasmelone.basicmodinfoparser;

import me.andreasmelone.basicmodinfoparser.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class BasicModInfo {
    private static final BasicModInfo EMPTY = new BasicModInfo(null, null, null, null, new Dependency[0]);

    private final String id;
    private final String name;
    private final Version<?> version;
    private final String description;
    private final Dependency[] dependencies;

    public BasicModInfo(@Nullable String id, @Nullable String name, @Nullable Version<?> version, @Nullable String description,
                        @NotNull Dependency[] dependencies) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.dependencies = dependencies;
    }

    /**
     * The mod id, which is usually required to be ^[a-z][a-z0-9_]{1,63}$
     * @return the mod id
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * The name of the mod
     * @return the name of the mod
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * The version of the mod. In difference to {@link Dependency}, this is not a version range
     * @return the version of the mod
     */
    @Nullable
    public Version<?> getVersion() {
        return version;
    }

    /**
     * The mods description
     * @return the mods description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * The mods dependencies
     * @return the mods dependencies
     * @see Dependency
     */
    @NotNull
    public Dependency[] getDependencies() {
        return dependencies;
    }

    /**
     * Whether this BasicModInfo object is empty
     * @return whether this is empty
     */
    public boolean isEmpty() {
        if(this == EMPTY) return true;
        return id == null
                && name == null
                && version == null
                && description == null
                && (dependencies == null || dependencies.length == 0);
    }

    @Override
    public String toString() {
        return "BasicModInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", description='" + description + '\'' +
                ", dependencies=" + Arrays.toString(dependencies) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BasicModInfo that = (BasicModInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(description, that.description) && Objects.deepEquals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, description, Arrays.hashCode(dependencies));
    }

    @NotNull
    public static BasicModInfo empty() {
        return EMPTY;
    }

    @NotNull
    public static BasicModInfo[] emptyArray() {
        return new BasicModInfo[0];
    }
}
