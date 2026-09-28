package arsipdelta.model;

import java.util.List;

public final class FullBackupJob extends BackupJob {
    @Override public String type() { return "FULL"; }
    @Override public BackupPlan createPlan(List<FileRecord> files, long version, BackupSnapshot baseline) {
        requireFiles(files);
        return new BackupPlan(type(), version, baseline == null ? null : baseline.id(),
            files.stream().map(f -> new BackupPlan.Entry(f, BackupPlan.Change.PENUH)).toList());
    }
}
