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
package me.andreasmelone.basicmodinfoparser.dependency.forge;

import me.andreasmelone.basicmodinfoparser.dependency.StandardDependency;

import java.util.Objects;

public class ForgeDependency extends StandardDependency {
    private final Ordering ordering;
    private final DependencySide side;

    public ForgeDependency(String id, String version, boolean mandatory, Ordering ordering, DependencySide side) {
        super(id, version, mandatory);
        this.ordering = ordering;
        this.side = side;
    }

    /**
     * When the dependency must be loaded
     * @return an element of the {@link Ordering} enum, signifying when the dependency must be loaded.
     * @see Ordering
     */
    public Ordering getOrdering() {
        return ordering;
    }

    /**
     * Where the dependency must be loaded
     * @return an element of the {@link DependencySide} enum, signifying on which side the dependency is needed.
     * @see DependencySide
     */
    public DependencySide getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "ForgeDependency{" +
                "id=" + getModId() +
                ", version=" + getRequiredVersion() +
                ", mandatory=" + isMandatory() +
                ", ordering=" + ordering +
                ", side=" + side +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForgeDependency that = (ForgeDependency) o;
        return ordering == that.ordering && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ordering, side);
    }
}
