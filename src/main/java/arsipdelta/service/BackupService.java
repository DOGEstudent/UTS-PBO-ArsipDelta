package arsipdelta.service;

import arsipdelta.model.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Service sinkron, dipanggil satu thread CLI. Commit melalui satu penggantian state. */
public final class BackupService {
    public static final int LIMIT = 100;
    private record State(BackupSnapshot baseline, List<BackupResult> history, int snapshots) { }
    private State state = new State(null, List.of(), 0);
    private final CatalogService catalog;
    private BackupPlan pending;
    public BackupService(CatalogService catalog) { this.catalog = Objects.requireNonNull(catalog); }
    public BackupSnapshot baseline() { return state.baseline(); }
    public List<BackupResult> history() { return state.history(); }
    public BackupPlan plan(boolean incremental) {
        pending = null;
        checkCapacity();
        BackupJob job = incremental ? new IncrementalBackupJob() : new FullBackupJob();
        pending = job.createPlan(catalog.files(), catalog.version(), state.baseline());
        return pending;
    }
    public void cancel() { pending = null; }
    private void checkCapacity() {
        if (state.history().size() >= LIMIT) throw new IllegalArgumentException("Batas 100 pekerjaan per sesi tercapai.");
    }
    public BackupResult execute(BackupPlan plan) {
        // Prasyarat penulisan tetap diperiksa di batas service, bukan hanya saat preview.
        checkCapacity();
        if (plan == null || pending != plan) throw new IllegalArgumentException("Rencana tidak aktif atau sudah dieksekusi.");
        pending = null;
        String baselineId = state.baseline() == null ? null : state.baseline().id();
        if (plan.catalogVersion() != catalog.version() || !Objects.equals(plan.baselineId(), baselineId))
            throw new IllegalArgumentException("Rencana kedaluwarsa. Buat preview baru.");
        boolean changed = plan.selectedCount() > 0;
        BackupSnapshot next = state.baseline();
        if (changed) next = new BackupSnapshot("SNP-%04d".formatted(state.snapshots() + 1),
            catalog.files().stream().collect(Collectors.toMap(FileRecord::id, f -> f)));
        BackupResult result = new BackupResult("JOB-%04d".formatted(state.history().size() + 1),
            changed ? BackupStatus.SUCCESS : BackupStatus.NO_CHANGES, LocalDateTime.now(), plan,
            changed ? next.id() : null);
        List<BackupResult> history = new ArrayList<>(state.history());
        history.add(result);
        State replacement = new State(next, List.copyOf(history), state.snapshots() + (changed ? 1 : 0));
        state = replacement;
        return result;
    }
}
