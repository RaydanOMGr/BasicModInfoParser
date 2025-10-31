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
package me.andreasmelone.basicmodinfoparser.platform.dependency;

import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.VersionRange;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface Dependency {
    /**
     * The ID of the dependency mod
     * @return The id of the dependency mod
     */
    String getModId();

    /**
     * The required version of this dependency, parsed as a {@link VersionRange}
     * @return The version range
     */
    VersionRange<?> getVersionRange();

    /**
     * Whether the dependency is required for the mod to run or not.
     * @return whether the dependency is mandatory (needed to run the mod)
     */
    boolean isMandatory();

    /**
     * @param mods a list of mods
     * @return checks whether this dependency is present in the list of mods
     */
    @NotNull
    PresenceStatus isPresent(List<BasicModInfo> mods);

    /**
     * @param mods an array of mods
     * @return checks whether this dependency is present in the list of mods
     */
    @NotNull
    default PresenceStatus isPresent(BasicModInfo[] mods) {
        return isPresent(Arrays.asList(mods));
    }
}
