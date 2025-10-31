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
package me.andreasmelone.basicmodinfoparser.platform.dependency.forge;

/**
 * When the dependency must be loaded. {@link Ordering#BEFORE} means it must be loaded before the mod, {@link Ordering#AFTER} means it must be loaded
 * after the mod, {@link Ordering#NONE} means the order of loading does not matter.
 */
public enum Ordering {
    /**
     * Dependency must be loaded before the mod
     */
    BEFORE,

    /**
     * Dependency must be loaded after the mod
     */
    AFTER,

    /**
     * Order loading of the dependency does not matter
     */
    NONE;

    public static Ordering getFromString(String ordering) {
        if (ordering.equalsIgnoreCase("before")
                || ordering.equalsIgnoreCase("required-before")) {
            return Ordering.BEFORE;
        } else if(ordering.equalsIgnoreCase("after")
                || ordering.equalsIgnoreCase("required-after")) {
            return Ordering.AFTER;
        }

        return Ordering.NONE;
    }
}
