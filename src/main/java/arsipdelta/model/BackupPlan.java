package arsipdelta.model;

import java.util.List;

public record BackupPlan(String type, long catalogVersion, String baselineId, List<Entry> entries) {
    public enum Change { BARU, BERUBAH, DILEWATI, PENUH }
    public record Entry(FileRecord file, Change change) { }
    public BackupPlan { entries = List.copyOf(entries); }
    public long selectedCount() { return entries.stream().filter(e -> e.change() != Change.DILEWATI).count(); }
    public long totalKb() { return entries.stream().filter(e -> e.change() != Change.DILEWATI).mapToLong(e -> e.file().sizeKb()).sum(); }
}
