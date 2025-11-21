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
package me.andreasmelone.basicmodinfoparser.platform.dependency;

import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.VersionRange;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.ProvidesList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StandardDependency<T extends Version> implements Dependency {
    protected final String id;
    protected final boolean mandatory;
    protected final VersionRange<T> range;

    public StandardDependency(String id, boolean mandatory, VersionRange<T> range) {
        this.id = id;
        this.mandatory = mandatory;
        this.range = range;
    }

    @Override
    public String getModId() {
        return id;
    }

    @Override
    public VersionRange<T> getVersionRange() {
        return range;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    // beautiful
    @Override
    public @NotNull PresenceStatus isPresent(List<BasicModInfo> mods) {
        for (BasicModInfo mod : mods) {
            if (mod instanceof ProvidesList) {
                ProvidesList<?> providesList = (ProvidesList<?>) mod;
                if (this.range == null || providesList.getType().isAssignableFrom(range.getType())) {
                    List<ProvidedMod> innerMods = new ArrayList<>(Optional.ofNullable(((ProvidesList<T>) providesList).getProvidedIds())
                            .orElse(Collections.emptyList()));

                    for (ProvidedMod innerMod : innerMods) {
                        if (innerMod.getId() == null || !innerMod.getId().equalsIgnoreCase(this.getModId())) continue;

                        if (innerMod.getVersion() == null || this.range == null || !range.getType().isInstance(mod.getVersion())) {
                            return PresenceStatus.PRESENT;
                        }

                        if (!range.contains(range.getType().cast(mod.getVersion()))) {
                            return PresenceStatus.VERSION_MISMATCH;
                        }

                        return PresenceStatus.PRESENT;
                    }
                }
            }

            if (mod == null || mod.getId() == null || !mod.getId().equalsIgnoreCase(this.getModId())) continue;

            if (mod.getVersion() == null || this.range == null || !range.getType().isInstance(mod.getVersion())) {
                return PresenceStatus.PRESENT;
            }

            if (!range.contains(range.getType().cast(mod.getVersion()))) {
                return PresenceStatus.VERSION_MISMATCH;
            }

            return PresenceStatus.PRESENT;
        }

        return PresenceStatus.NOT_PRESENT;
    }

    @Override
    public String toString() {
        return "StandardDependency{" +
                "id='" + id + '\'' +
                ", mandatory=" + mandatory +
                ", range=" + range +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardDependency<?> that = (StandardDependency<?>) o;
        return mandatory == that.mandatory && Objects.equals(id, that.id) && Objects.equals(range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mandatory, range);
    }
}
