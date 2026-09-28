package arsipdelta.model;

import arsipdelta.validation.PathValidator;

public record FileRecord(String id, String path, long sizeKb, long revision) {
    public FileRecord {
        if (id == null || !id.matches("FIL-[0-9]{4,}")) throw new IllegalArgumentException("ID berkas invalid.");
        path = PathValidator.normalize(path);
        if (sizeKb < 1 || sizeKb > 1_000_000) throw new IllegalArgumentException("Ukuran harus 1-1000000 KB.");
        if (revision < 1) throw new IllegalArgumentException("Revision harus positif.");
    }
}
