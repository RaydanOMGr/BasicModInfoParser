# BasicModInfoParser
BasicModInfoParser is a lightweight Java library that helps you extract basic information from mod files `.jar` files for platforms like Minecraft Forge or Fabric. The library parses data such as mod ID, name, version, description, and dependencies, making it easier to handle mod metadata programmatically.

## Features
- Supports detecting mod platforms (Forge, Fabric, Quilt).
- Parses mod metadata, including:
  - Mod ID
  - Mod Name
  - Version
  - Description
  - Dependencies

## Installation

To include this library in your project, follow these steps:

1. Add the Radsteve's repository to your project's `build.gradle(.kts)` file (or equivalent for other build systems).

### Gradle

```groovy
repositories {
    maven {
        name "radRepo"
        url "https://maven.radsteve.net/public"
    }
}

dependencies {
    implementation("me.andreasmelone:BasicModInfoParser:2.0.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>rad-repo</id>
        <name>rad's maven</name>
        <url>https://maven.radsteve.net/public</url>
    </repository>
</repositories>

<dependency>
    <groupId>me.andreasmelone</groupId>
    <artifactId>BasicModInfoParser</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Usage

Here's a simple example demonstrating how to use `BasicModInfoParser` to extract mod information from `.jar` files:

```java
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.StandardBasicModInfo;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class ModInfoExample {
    public static void main(String[] args) {
        // Specify the folder containing your .jar files
        File folder = new File("mods/");
        File[] modFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (modFiles != null) {
            for (File modFile : modFiles) {
                try (JarFile jarFile = new JarFile(modFile)) {
                    // Detect the mod platform (Forge, Fabric, etc.)
                    Platform[] platforms = Platform.findModPlatform(modFile);
                    if (platforms.length == 0) {
                      System.out.println("No supported platform found for: " + modFile.getName());
                      continue;
                    }
                    for (Platform platform : platforms) {
                        // Get the mod info content and parse it
                        String modInfoContent = platform.getInfoFileContent(jarFile);
                        for(BasicModInfo modInfo : platform.parse(modInfoContent)) {
                          // Output the parsed mod information
                          System.out.println("Mod ID: " + modInfo.getId());
                          System.out.println("Mod Name: " + modInfo.getName());
                          System.out.println("Mod Version: " + modInfo.getVersion());
                          System.out.println("Mod Description: " + modInfo.getDescription());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read mod file: " + modFile.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
```

### BasicModInfo Object

The `BasicModInfo` object stores the following details about a mod:
- **id**: The unique identifier for the mod.
- **name**: The name of the mod.
- **version**: The version of the mod.
- **description**: A brief description of the mod.
- **dependencies**: The dependencies the mod requires or recommends to be installed to run correctly.

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
