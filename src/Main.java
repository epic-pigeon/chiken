import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java Main.class compile|c|decompile|d|run|r ...");
            System.exit(1);
        }
        String method = args[0];
        if (method.equals("compile") || method.equals("c")) {
            if (args.length < 3) {
                System.err.println("Usage: java Main.class compile|c source.chiken Result.class");
                System.exit(1);
            }
            String source = args[1];
            String dest = args[2];
            String code = new String(Files.readAllBytes(Paths.get(source)));
            byte[] result = chikenToBytes(code);
            Files.write(Paths.get(dest), result);
        } else if (method.equals("decompile") || method.equals("d")) {
            if (args.length < 3) {
                System.err.println("Usage: java Main.class decompile|d Source.class result.chiken");
                System.exit(1);
            }
            String source = args[1];
            String dest = args[2];
            byte[] bytes = Files.readAllBytes(Paths.get(source));
            String code = bytesToChiken(bytes);
            Files.write(Paths.get(dest), code.getBytes());
        } else if (method.equals("run") || method.equals("r")) {
            if (args.length < 2) {
                System.err.println("Usage: java Main.class run|r source.chiken args...");
                System.exit(1);
            }
            String source = args[1];
            String code = new String(Files.readAllBytes(Paths.get(source)));
            String[] chikenArgs = new String[args.length - 2];
            for (int i = 0; i < args.length - 2; i++) {
                chikenArgs[i] = args[i+2];
            }
            runChiken(code, chikenArgs);
        }
    }
    private static byte[] chikenToBytes(String code) {
        String[] lines = code.split("\\r?\\n");
        byte[] result = new byte[lines.length];
        for (int i = 0; i < lines.length; i++) {
            byte a = 0;
            if (lines[i].length() > 0) {
                String[] words = lines[i].split(" +");
                for (String word : words) {
                    if (!word.equals("chiken")) {
                        System.out.println(lines[i]);
                        throw new RuntimeException("'" + word + "' is not chiken!!");
                    }
                    a++;
                }
            }
            result[i] = a;
        }
        return result;
    }
    private static void runChiken(String code, String... args) {
        byte[] bytes = chikenToBytes(code);
        Class clazz = ByteCodeLoader.instance.loadClass(bytes);
        try {
            clazz.getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    private static String bytesToChiken(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) result.append("\n");
            int count = bytes[i];
            if (count < 0) count += 256;
            for (int j = 0; j < count; j++) {
                if (j != 0) result.append(" ");
                result.append("chiken");
            }
        }
        return result.toString();
    }
}

class ByteCodeLoader extends ClassLoader {
    public static final ByteCodeLoader instance = new ByteCodeLoader();

    public Class<?> loadClass(byte[] bytecode) {
        return defineClass(null, bytecode, 0, bytecode.length);
    }
}
