package com.pointofsale;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path javaExe = javaHome.resolve("bin").resolve("java.exe");
        String appPath = System.getProperty("jpackage.app-path");
        Path appRoot = appPath == null ? null : Paths.get(appPath).getParent();
        Path root = Paths.get(System.getenv("LOCALAPPDATA"), "PointOfSale");

        // 1. Try to update (best effort, max 45 s)
        try {
            Path script = appRoot == null ? null : appRoot.resolve("staging-content").resolve("Start-POS.ps1");
            if (script != null && Files.exists(script)) {
                Process p = new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
                        "-WindowStyle", "Hidden", "-File", script.toString(),
                        "-CheckOnly", "-JavaExe", javaExe.toString())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
                if (!p.waitFor(45, TimeUnit.SECONDS)) p.destroyForcibly();
            }
        } catch (Exception ignored) { }

        // 2. Run the updated jar if present, otherwise the bundled one
        try {
            Path pointer = root.resolve("current.json");
            if (Files.exists(pointer)) {
                JsonObject o = JsonParser.parseString(Files.readString(pointer)).getAsJsonObject();
                Path jar = root.resolve("versions").resolve(o.get("tag").getAsString()).resolve("PointOfSale.jar");
                if (Files.exists(jar)) {
                    Process p = new ProcessBuilder(javaExe.toString(), "-jar", jar.toString())
                            .inheritIO().start();
                    System.exit(p.waitFor());
                }
            }
        } catch (Exception ignored) { }
        App.main(args);
    }
}