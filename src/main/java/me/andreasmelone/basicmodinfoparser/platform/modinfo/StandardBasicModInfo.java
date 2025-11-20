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
package me.andreasmelone.basicmodinfoparser.platform.modinfo;

import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StandardBasicModInfo implements BasicModInfo {
    private final String id;
    private final String name;
    private final Version version;
    private final String description;
    private final List<Dependency> dependencies;
    private final String iconPath;
    private final Platform platform;
    private final List<String> authors;

    public StandardBasicModInfo(
            @Nullable String id,
            @Nullable String name,
            @Nullable Version version,
            @Nullable String description,
            @Nullable List<Dependency> dependencies,
            @Nullable String iconPath,
            @NotNull Platform platform,
            @Nullable List<String> authors
    ) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : null;
        this.iconPath = iconPath;
        this.platform = platform;
        this.authors = authors;
    }

    /**
     * The mod id, which is usually required to be ^[a-z][a-z0-9_]{1,63}$
     *
     * @return the mod id
     */
    @Override
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * The name of the mod
     *
     * @return the name of the mod
     */
    @Override
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * The version of the mod. In difference to {@link Dependency}, this is not a version range
     *
     * @return the version of the mod
     */
    @Override
    @Nullable
    public Version getVersion() {
        return version;
    }

    /**
     * The mods description
     *
     * @return the mods description
     */
    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * The mods dependencies
     *
     * @return the mods dependencies
     * @see Dependency
     */
    @Override
    @NotNull
    public List<Dependency> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    @Override
    public @Nullable String getIconPath() {
        return iconPath;
    }

    @Override
    public @NotNull Platform getPlatform() {
        return platform;
    }

    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "StandardBasicModInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", description='" + description + '\'' +
                ", dependencies=" + dependencies +
                ", iconPath='" + iconPath + '\'' +
                ", platform=" + platform +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardBasicModInfo that = (StandardBasicModInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(description, that.description) && Objects.equals(dependencies, that.dependencies) && Objects.equals(iconPath, that.iconPath) && platform == that.platform;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, version, description, dependencies, iconPath, platform);
    }

    @NotNull
    public static StandardBasicModInfo[] emptyArray() {
        return new StandardBasicModInfo[0];
    }

}
