package me.andreasmelone.basicmodinfoparser.test;

import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.LooseSemanticVersion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LooseSemanticVersionParserTests {
    @Nested
    class Basic {
        @Test
        void parsesSimpleVersion() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
        }

        @Test
        void parsesWithMetadata() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0+metadata");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertEquals("metadata", ver.get().getBuildMetadata());
        }
    }

    @Nested
    class PreRelease {
        @Test
        void parsesWithoutNumber() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0-alpha");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
        }

        @Test
        void parsesWithNumber() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0-alpha.1");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertEquals(1, ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
        }

        @Test
        void parsesWithEmptyPreRelease() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.2-");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 2 }, ver.get().getVersionParts());
            assertEquals("", ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
        }
    }

    @Nested
    class Wildcard {
        @Test
        void parsesLowercaseXWildcard() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.2.x", true);
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 2, 0 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
            assertArrayEquals(new Integer[] { 2 }, ver.get().getWildcardPositions().toArray(new Integer[0]));
        }

        @Test
        void parsesUppercaseXWildcard() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.2.X", true);
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 2, 0 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
            assertArrayEquals(new Integer[] { 2 }, ver.get().getWildcardPositions().toArray(new Integer[0]));
        }

        @Test
        void parsesAsterixWildcard() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.2.*", true);
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 2, 0 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
            assertArrayEquals(new Integer[] { 2 }, ver.get().getWildcardPositions().toArray(new Integer[0]));
        }

        @Test
        void parsesWithMiddleWildcard() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.x.4", true);
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 4 }, ver.get().getVersionParts());
            assertNull(ver.get().getPreReleaseSuffix());
            assertNull(ver.get().getPreReleaseNumber());
            assertNull(ver.get().getBuildMetadata());
            assertArrayEquals(new Integer[] { 1 }, ver.get().getWildcardPositions().toArray(new Integer[0]));
        }
    }

    @Nested
    class General {
        @Test
        void parsesOrdinarySemVer() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0-alpha.1+metadata");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertEquals(1, ver.get().getPreReleaseNumber());
            assertEquals("metadata", ver.get().getBuildMetadata());
        }

        @Test
        void parsesWithLessComponents() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0-alpha.1+metadata");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertEquals(1, ver.get().getPreReleaseNumber());
            assertEquals("metadata", ver.get().getBuildMetadata());
        }

        @Test
        void parsesWithMoreComponents() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0.0-alpha.1+metadata");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 1, 0, 0, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertEquals(1, ver.get().getPreReleaseNumber());
            assertEquals("metadata", ver.get().getBuildMetadata());
        }

        @Test
        void parsesRealVersion() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("11.0.0-alpha.3+0.102.0-1.21");
            assertTrue(ver.isPresent());
            assertArrayEquals(new int[] { 11, 0, 0 }, ver.get().getVersionParts());
            assertEquals("alpha", ver.get().getPreReleaseSuffix());
            assertEquals(3, ver.get().getPreReleaseNumber());
            assertEquals("0.102.0-1.21", ver.get().getBuildMetadata());
        }
    }

    @Nested
    class Invalid {
        @Test
        void rejectsGarbage() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("potato");
            assertFalse(ver.isPresent());
        }

        @Test
        void rejectsEmptyString() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("");
            assertFalse(ver.isPresent());
        }

        @Test
        void rejectsNonAlphanumeric() {
            Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0-абрикос.2+不甜瓜");
            assertFalse(ver.isPresent());
        }
    }
}
