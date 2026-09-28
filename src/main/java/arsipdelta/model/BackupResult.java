package arsipdelta.model;

import java.time.LocalDateTime;

public record BackupResult(String id, BackupStatus status, LocalDateTime time, BackupPlan plan, String snapshotId) { }
