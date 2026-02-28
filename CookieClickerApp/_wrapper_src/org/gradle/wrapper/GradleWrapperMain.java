package org.gradle.wrapper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        File projectDir = new File(System.getProperty("user.dir"));
        File propsFile = new File(projectDir, "gradle/wrapper/gradle-wrapper.properties");
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(propsFile)) {
            props.load(in);
        }

        String distUrl = require(props, "distributionUrl");
        String distBase = props.getProperty("distributionBase", "GRADLE_USER_HOME").trim();
        String distPath = props.getProperty("distributionPath", "wrapper/dists").trim();

        File baseDir = "PROJECT".equalsIgnoreCase(distBase) ? projectDir : new File(System.getProperty("user.home"), ".gradle");
        File distsDir = new File(baseDir, distPath);
        distsDir.mkdirs();

        String distFileName = distUrl.substring(distUrl.lastIndexOf('/') + 1);
        String distId = distFileName.replace(".zip", "");
        String hash = sha256(distUrl);
        File installDir = new File(new File(distsDir, distId), hash);
        File marker = new File(installDir, ".installed");

        if (!marker.exists()) {
            installDir.mkdirs();
            File zipFile = new File(installDir, distFileName);
            if (!zipFile.exists() || zipFile.length() == 0) {
                download(distUrl, zipFile);
            }
            unzip(zipFile, installDir);
            try (FileWriter fw = new FileWriter(marker)) {
                fw.write("ok");
            }
        }

        File gradleHome = findGradleHome(installDir);
        if (gradleHome == null) {
            System.err.println("Could not locate Gradle home under: " + installDir.getAbsolutePath());
            System.exit(1);
            return;
        }

        File launcherJar = findLauncherJar(new File(gradleHome, "lib"));
        if (launcherJar == null) {
            System.err.println("Could not locate gradle-launcher jar under: " + gradleHome.getAbsolutePath());
            System.exit(1);
            return;
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(findJavaBin());
        cmd.add("-classpath");
        cmd.add(launcherJar.getAbsolutePath());
        cmd.add("org.gradle.launcher.GradleMain");
        cmd.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        pb.environment().put("GRADLE_HOME", gradleHome.getAbsolutePath());
        Process p = pb.start();
        int code = p.waitFor();
        System.exit(code);
    }

    private static String require(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.trim().isEmpty()) throw new IllegalStateException("Missing " + key);
        return v.trim();
    }

    private static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static void download(String url, File out) throws Exception {
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.connect();
        int code = c.getResponseCode();
        if (code >= 400) throw new IOException("HTTP " + code + " for " + url);
        try (InputStream in = c.getInputStream(); OutputStream os = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) os.write(buf, 0, r);
        } finally {
            c.disconnect();
        }
    }

    private static void unzip(File zip, File toDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                File out = new File(toDir, e.getName());
                if (e.isDirectory()) {
                    out.mkdirs();
                } else {
                    File parent = out.getParentFile();
                    if (parent != null) parent.mkdirs();
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = zis.read(buf)) != -1) os.write(buf, 0, r);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static File findGradleHome(File installDir) {
        File[] kids = installDir.listFiles();
        if (kids == null) return null;
        for (File f : kids) {
            if (f.isDirectory() && f.getName().startsWith("gradle-")) {
                return f;
            }
        }
        return null;
    }

    private static File findLauncherJar(File libDir) {
        File[] kids = libDir.listFiles();
        if (kids == null) return null;
        for (File f : kids) {
            if (f.isFile() && f.getName().startsWith("gradle-launcher-") && f.getName().endsWith(".jar")) {
                return f;
            }
        }
        return null;
    }

    private static String findJavaBin() {
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.trim().isEmpty()) {
            File bin = new File(javaHome, "bin/java");
            if (bin.exists()) return bin.getAbsolutePath();
            File binExe = new File(javaHome, "bin/java.exe");
            if (binExe.exists()) return binExe.getAbsolutePath();
        }
        return "java";
    }
}
