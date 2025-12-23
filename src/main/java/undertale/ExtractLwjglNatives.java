package undertale;
import java.io.*;
import java.nio.file.*;
import java.util.jar.*;

public class ExtractLwjglNatives {
    public static void main(String[] args) throws IOException {
        Path nativesDir = Paths.get("target/natives");
        if (!Files.exists(nativesDir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(nativesDir, "*.jar")) {
            for (Path jarPath : stream) {
                try (JarFile jar = new JarFile(jarPath.toFile())) {
                    jar.stream().forEach(entry -> {
                        if (!entry.isDirectory() && (entry.getName().endsWith(".dll") || entry.getName().endsWith(".pdb"))) {
                            Path out = nativesDir.resolve(Paths.get(entry.getName()).getFileName().toString());
                            try (InputStream in = jar.getInputStream(entry)) {
                                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }
}
