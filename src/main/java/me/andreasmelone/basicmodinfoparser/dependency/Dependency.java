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

public interface Dependency {
    /**
     * The ID of the dependency mod
     * @return The id of the dependency mod
     */
    String getModId();

    /**
     * The required version, which is usually a <a href="https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html">Maven Version Range</a> when working with forge, whereas
     * fabric uses their own <a href="https://wiki.fabricmc.net/documentation:fabric_mod_json_spec#versionrange">format</a>.
     * @return The version range, or null, if none is specified. None being specified usually means any version will do.
     */
    String getRequiredVersion();

    /**
     * Whether the dependency is required for the mod to run or not.
     * @return whether the dependency is mandatory (needed to run the mod)
     */
    boolean isMandatory();
}
