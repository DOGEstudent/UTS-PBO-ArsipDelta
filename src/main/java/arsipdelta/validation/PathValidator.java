package arsipdelta.validation;

import java.util.Locale;

public final class PathValidator {
    private PathValidator() { }
    public static String normalize(String value) {
        if (value == null) throw new IllegalArgumentException("Path wajib diisi.");
        if (value.codePoints().anyMatch(c -> Character.isISOControl(c) || Character.getType(c) == Character.FORMAT))
            throw new IllegalArgumentException("Path tidak boleh mengandung karakter kontrol.");
        String path = value.trim().replace('\\', '/');
        if (path.isEmpty() || path.length() > 120 || path.startsWith("/") || path.matches(".*[:*?\"<>|].*"))
            throw new IllegalArgumentException("Gunakan path relatif 1-120 karakter tanpa : * ? \" < > |.");
        for (String part : path.split("/", -1))
            if (part.isBlank() || part.equals(".") || part.equals("..") || !part.equals(part.trim()))
                throw new IllegalArgumentException("Segmen path kosong, titik, atau spasi tepi tidak diizinkan.");
        return path;
    }
    public static String key(String path) { return normalize(path).toLowerCase(Locale.ROOT); }
}
