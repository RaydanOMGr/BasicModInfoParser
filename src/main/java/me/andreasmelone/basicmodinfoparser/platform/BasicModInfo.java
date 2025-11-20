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
package me.andreasmelone.basicmodinfoparser.platform;

import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BasicModInfo {
    /**
     * The mod id, which is usually required to be ^[a-z][a-z0-9_]{1,63}$
     *
     * @return the mod id
     */
    @Nullable String getId();

    /**
     * The name of the mod
     *
     * @return the name of the mod
     */
    @Nullable String getName();

    /**
     * The version of the mod. In difference to {@link Dependency}, this is not a version range
     *
     * @return the version of the mod
     */
    @Nullable Version getVersion();

    /**
     * The mods description
     *
     * @return the mods description
     */
    @Nullable String getDescription();

    /**
     * The mods dependencies
     *
     * @return the mods dependencies
     * @see Dependency
     */
    @NotNull List<Dependency> getDependencies();

    /**
     * @return the path to the icon in the jarfile
     */
    @Nullable String getIconPath();

    /**
     * @return the platform that this mod is for
     */
    @NotNull Platform getPlatform();
}
