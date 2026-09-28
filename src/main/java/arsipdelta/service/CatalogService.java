package arsipdelta.service;

import arsipdelta.model.FileRecord;
import arsipdelta.validation.PathValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatalogService {
    public static final int LIMIT = 500;
    private final Map<String, FileRecord> files = new LinkedHashMap<>();
    private long version;
    public List<FileRecord> files() { return List.copyOf(files.values()); }
    public long version() { return version; }
    public void validateNewPath(String path) {
        if (files.size() >= LIMIT) throw new IllegalArgumentException("Kapasitas katalog 500 berkas tercapai.");
        String key = PathValidator.key(path);
        if (files.values().stream().anyMatch(f -> PathValidator.key(f.path()).equals(key)))
            throw new IllegalArgumentException("Path sudah ada. Gunakan menu Perbarui Berkas.");
    }
    public FileRecord add(String path, long size) {
        validateNewPath(path);
        FileRecord file = new FileRecord("FIL-%04d".formatted(files.size() + 1), path, size, 1);
        files.put(file.id(), file);
        version++;
        return file;
    }
    public FileRecord find(String id) {
        FileRecord file = files.get(id == null ? "" : id.trim().toUpperCase(java.util.Locale.ROOT));
        if (file == null) throw new IllegalArgumentException("ID berkas tidak ditemukan.");
        return file;
    }
    public FileRecord update(String id, long size) {
        FileRecord old = find(id);
        FileRecord replacement = new FileRecord(old.id(), old.path(), size, Math.incrementExact(old.revision()));
        files.put(old.id(), replacement);
        version++;
        return replacement;
    }
}
