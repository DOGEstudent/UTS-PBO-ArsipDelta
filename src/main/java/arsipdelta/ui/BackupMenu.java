package arsipdelta.ui;

import arsipdelta.model.*;
import arsipdelta.service.BackupService;
import java.io.PrintWriter;

public final class BackupMenu {
    private final BackupService backup;
    private final ConsoleInput input;
    private final ConsolePrinter printer;
    private final PrintWriter out;
    public BackupMenu(BackupService backup, ConsoleInput input, ConsolePrinter printer, PrintWriter out) {
        this.backup = backup; this.input = input; this.printer = printer; this.out = out;
    }
    public void run() {
        try {
            out.println("\nJENIS BACKUP\n1. Full\n2. Incremental");
            int kind = input.number("Pilihan: ", 1, 2);
            BackupPlan plan = backup.plan(kind == 2);
            printer.plan(plan);
            if (!input.confirm("Jalankan simulasi sesuai preview?")) { out.println("Preview dibatalkan."); return; }
            out.println("[1/2] Memvalidasi rencana dan menyiapkan snapshot simulasi...");
            BackupResult result = backup.execute(plan);
            out.println("[2/2] Hasil simulasi selesai. Tidak ada file asli yang disalin.");
            printer.result(result);
        } finally { backup.cancel(); }
    }
    public void history() {
        printer.history(backup.history());
        if (backup.history().isEmpty()) return;
        BackupResult result = input.ask("ID pekerjaan untuk detail (/batal untuk kembali): ", id -> backup.history().stream()
            .filter(r -> r.id().equalsIgnoreCase(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("ID pekerjaan tidak ditemukan.")));
        printer.result(result);
        printer.plan(result.plan());
    }
}
