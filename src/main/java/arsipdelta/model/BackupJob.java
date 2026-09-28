package arsipdelta.model;

import java.util.List;

/** Kontrak algoritma; state pekerjaan disimpan terpisah dalam BackupResult. */
public abstract class BackupJob {
    public abstract String type();
    public abstract BackupPlan createPlan(List<FileRecord> files, long version, BackupSnapshot baseline);
    protected void requireFiles(List<FileRecord> files) {
        if (files.isEmpty()) throw new IllegalArgumentException("Katalog kosong. Tambahkan berkas terlebih dahulu.");
    }
}
