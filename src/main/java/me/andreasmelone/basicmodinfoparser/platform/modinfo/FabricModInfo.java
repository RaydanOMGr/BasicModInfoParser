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

public class FabricModInfo extends StandardBasicModInfo implements BreaksList, ProvidesList<LooseSemanticVersion> {
    private final List<Dependency> breaks;
    private final List<ProvidedMod<LooseSemanticVersion>> provides;

    public FabricModInfo(@Nullable String id, @Nullable String name, @Nullable Version<?> version, @Nullable String description, @Nullable List<Dependency> dependencies, @Nullable String iconPath, @NotNull Platform platform, @Nullable List<Dependency> breaks, @Nullable List<ProvidedMod<LooseSemanticVersion>> provides) {
        super(id, name, version, description, dependencies, iconPath, platform);
        this.breaks = breaks != null ? new ArrayList<>(breaks) : null;
        this.provides = provides != null ? new ArrayList<>(provides) : null;
    }

    @Override
    public List<Dependency> getBreaks() {
        if(breaks == null) return null;
        return new ArrayList<>(breaks);
    }

    @Override
    public List<ProvidedMod<LooseSemanticVersion>> getProvidedIds() {
        if(provides == null) return null;
        return new ArrayList<>(provides);
    }

    @Override
    public Class<LooseSemanticVersion> getType() {
        return LooseSemanticVersion.class;
    }
}
