package me.andreasmelone.basicmodinfoparser.platform.modinfo;

import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;

import java.util.List;

public interface BreaksList {
    /**
     * @return a list of {@link Dependency} that this mod is incompatible with
     */
    List<Dependency> getBreaks();
}
