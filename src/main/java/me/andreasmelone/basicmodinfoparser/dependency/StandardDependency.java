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
package me.andreasmelone.basicmodinfoparser.dependency;

import me.andreasmelone.basicmodinfoparser.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.dependency.version.VersionRange;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StandardDependency implements Dependency {
    protected final String id;
    protected final boolean mandatory;
    protected final VersionRange<?> range;

    public StandardDependency(String id, boolean mandatory, VersionRange<?> range) {
        this.id = id;
        this.mandatory = mandatory;
        this.range = range;
    }

    @Override
    public String getModId() {
        return id;
    }

    @Override
    public VersionRange<?> getVersionRange() {
        return range;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public @NotNull PresenceStatus isPresent(List<BasicModInfo> mods) {
        return PresenceStatus.NOT_PRESENT;
    }

    @Override
    public @NotNull PresenceStatus isPresent(BasicModInfo[] mods) {
        return isPresent(Arrays.asList(mods));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardDependency that = (StandardDependency) o;
        return mandatory == that.mandatory && Objects.equals(id, that.id) && Objects.equals(range, that.range);
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
    public int hashCode() {
        return Objects.hash(id, mandatory, range);
    }
}
