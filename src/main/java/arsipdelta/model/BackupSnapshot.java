package arsipdelta.model;

import java.util.Map;

public record BackupSnapshot(String id, Map<String, FileRecord> files) {
    public BackupSnapshot { files = Map.copyOf(files); }
}
