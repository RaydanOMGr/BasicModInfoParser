package me.andreasmelone.basicmodinfoparser.platform.dependency.forge;

import me.andreasmelone.basicmodinfoparser.platform.dependency.version.VersionRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenVersionRange implements VersionRange<MavenVersion> {
    private static final Pattern RANGE_PATTERN = Pattern.compile("\\s*([\\[(])([^,]*),([^])]*)([])])\\s*");

    private final String stringVersion;
    private final Range[] ranges;

    public MavenVersionRange(String stringVersion, Range... ranges) {
        this.stringVersion = stringVersion;
        this.ranges = ranges;
    }

    @Override
    public String getStringRepresentation() {
        return stringVersion;
    }

    @Override
    public boolean contains(MavenVersion version) {
        for (Range range : ranges) {
            if(!range.contains(version)) return false;
        }
        return true;
    }

    @Override
    public Class<MavenVersion> getType() {
        return MavenVersion.class;
    }

    @Override
    public String toString() {
        return "MavenVersionRange{" +
                "stringVersion='" + stringVersion + '\'' +
                ", ranges=" + Arrays.toString(ranges) +
                '}';
    }

    public static Optional<MavenVersionRange> parse(String range) {
        if(range == null) return Optional.empty();

        List<Range> ranges = new ArrayList<>();

        String[] parts = range.split("(?<=]),|(?<=\\))\\s*,\\s*(?=[\\[(])");
        for (String part : parts) {
            Matcher matcher = RANGE_PATTERN.matcher(part);
            if (matcher.matches()) {
                boolean lowerExclusive = "(".equals(matcher.group(1));
                boolean upperExclusive = ")".equals(matcher.group(4));

                String lowerStr = matcher.group(2).trim();
                String upperStr = matcher.group(3).trim();

                MavenVersion lower = lowerStr.isEmpty() ? null : MavenVersion.parse(lowerStr).orElse(null);
                MavenVersion upper = upperStr.isEmpty() ? null : MavenVersion.parse(upperStr).orElse(null);

                ranges.add(new Range(lower, lowerExclusive, upper, upperExclusive));
            } else {
                Optional<MavenVersion> exact = MavenVersion.parse(part.trim());
                exact.ifPresent(v -> ranges.add(new Range(v, false, v, false)));
            }
        }

        if(ranges.isEmpty()) return Optional.empty();
        return Optional.of(new MavenVersionRange(range, ranges.toArray(new Range[0])));
    }

    public static class Range {
        private final MavenVersion lowerBound;
        private final boolean lowerExclusive;
        private final MavenVersion upperBound;
        private final boolean upperExclusive;

        public Range(MavenVersion lowerBound, boolean lowerExclusive, MavenVersion upperBound, boolean upperExclusive) {
            this.lowerBound = lowerBound;
            this.lowerExclusive = lowerExclusive;
            this.upperBound = upperBound;
            this.upperExclusive = upperExclusive;
        }

        public boolean contains(MavenVersion version) {
            if(lowerBound != null && lowerExclusive && version.compareTo(lowerBound) == 0) return false;
            if(upperBound != null && upperExclusive && version.compareTo(upperBound) == 0) return false;

            if(lowerBound != null && version.compareTo(lowerBound) < 0) return false;
            return upperBound != null &&  version.compareTo(upperBound) > 0;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "lowerBound=" + lowerBound +
                    ", lowerExclusive=" + lowerExclusive +
                    ", upperBound=" + upperBound +
                    ", upperExclusive=" + upperExclusive +
                    '}';
        }
    }
}
