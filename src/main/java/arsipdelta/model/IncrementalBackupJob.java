package arsipdelta.model;

import java.util.List;

public final class IncrementalBackupJob extends BackupJob {
    @Override public String type() { return "INCREMENTAL"; }
    @Override public BackupPlan createPlan(List<FileRecord> files, long version, BackupSnapshot baseline) {
        requireFiles(files);
        if (baseline == null) throw new IllegalArgumentException("Belum ada baseline. Jalankan full backup.");
        return new BackupPlan(type(), version, baseline.id(), files.stream().map(f -> {
            FileRecord old = baseline.files().get(f.id());
            BackupPlan.Change change = old == null ? BackupPlan.Change.BARU
                : old.revision() != f.revision() ? BackupPlan.Change.BERUBAH : BackupPlan.Change.DILEWATI;
            return new BackupPlan.Entry(f, change);
        }).toList());
    }
}
