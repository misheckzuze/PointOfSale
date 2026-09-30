package com.pointofsale;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.awt.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path javaExe = javaHome.resolve("bin").resolve("java.exe");
        String appPath = System.getProperty("jpackage.app-path");
        Path appRoot = appPath == null ? null : Paths.get(appPath).getParent();
        Path root = Paths.get(System.getenv("LOCALAPPDATA"), "PointOfSale");

        JWindow splash = showSplash("Checking for updates...");

        // 1. Try to update (best effort, max 3 min)
        try {
            Path script = appRoot == null ? null : appRoot.resolve("staging-content").resolve("Start-POS.ps1");
            if (script != null && Files.exists(script)) {
                Process p = new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
                        "-WindowStyle", "Hidden", "-File", script.toString(),
                        "-CheckOnly", "-JavaExe", javaExe.toString())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
                if (!p.waitFor(180, TimeUnit.SECONDS)) p.destroyForcibly();
            }
        } catch (Exception ignored) { }

        closeSplash(splash);

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

    private static JWindow showSplash(String message) {
        try {
            JWindow window = new JWindow();
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            panel.setBackground(Color.WHITE);
            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            panel.add(label, BorderLayout.CENTER);
            window.getContentPane().add(panel);
            window.setSize(280, 90);
            window.setLocationRelativeTo(null);
            window.setAlwaysOnTop(true);
            window.setVisible(true);
            return window;
        } catch (Exception e) {
            return null; // headless or display issue; fail silently, update still runs
        }
    }

    private static void closeSplash(JWindow window) {
        if (window != null) {
            window.setVisible(false);
            window.dispose();
        }
    }
}