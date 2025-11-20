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

import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.ProvidedMod;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.LooseSemanticVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabricModInfo extends StandardBasicModInfo implements ProvidesList<LooseSemanticVersion> {
    private final List<ProvidedMod> provides;

    public FabricModInfo(@Nullable String id, @Nullable String name, @Nullable Version<?> version, @Nullable String description, @Nullable List<Dependency> dependencies, @Nullable String iconPath, @NotNull Platform platform, @Nullable List<Dependency> breaks, @Nullable List<ProvidedMod<LooseSemanticVersion>> provides) {
        super(id, name, version, description, dependencies, iconPath, platform);
        this.breaks = breaks != null ? new ArrayList<>(breaks) : null;
        this.provides = provides != null ? new ArrayList<>(provides) : null;
    }

    @Override
    public List<ProvidedMod> getProvidedIds() {
        if (provides == null) return null;
        return new ArrayList<>(provides);
    }

    @Override
    public Class<LooseSemanticVersion> getType() {
        return LooseSemanticVersion.class;
    }

    @Override
    public String toString() {
        return "FabricModInfo{" +
                "breaks=" + breaks +
                ", provides=" + provides +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FabricModInfo that = (FabricModInfo) o;
        return Objects.equals(breaks, that.breaks) && Objects.equals(provides, that.provides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), breaks, provides);
    }
}
