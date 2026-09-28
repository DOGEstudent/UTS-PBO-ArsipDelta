package arsipdelta.ui;

import arsipdelta.model.*;
import java.io.PrintWriter;
import java.util.List;

public final class ConsolePrinter {
    private final PrintWriter out;
    private final ConsoleInput input;
    public ConsolePrinter(PrintWriter out, ConsoleInput input) { this.out = out; this.input = input; }
    public void pages(List<String> rows) {
        if (rows.isEmpty()) out.println("Belum ada data.");
        for (int i = 0; i < rows.size(); i++) {
            out.println(rows.get(i));
            if ((i + 1) % 15 == 0 && i + 1 < rows.size())
                if (!input.confirm("Lihat halaman berikutnya?")) break;
        }
    }
    public void files(List<FileRecord> files) {
        table(new String[]{"ID", "Path", "KB", "Revision"}, new int[]{8, 36, 7, 8},
            files.stream().map(f -> new String[]{f.id(), f.path(), "" + f.sizeKb(), "r" + f.revision()}).toList(), 2);
    }
    // Satu record tetap dihitung sebagai satu item pagination, walaupun path dibungkus.
    private void table(String[] headers, int[] widths, List<String[]> rows, int numeric) {
        if (rows.isEmpty()) { out.println("Belum ada data."); return; }
        String border = "+";
        for (int width : widths) border += "-".repeat(width + 2) + "+";
        for (int start = 0; start < rows.size(); start += 15) {
            out.println(border);
            tableRow(headers, widths, -1);
            out.println(border);
            int end = Math.min(start + 15, rows.size());
            for (int i = start; i < end; i++) tableRow(rows.get(i), widths, numeric);
            out.println(border);
            if (end < rows.size() && !input.confirm("Lihat halaman berikutnya?")) break;
        }
    }
    private void tableRow(String[] cells, int[] widths, int numeric) {
        int[][] points = java.util.Arrays.stream(cells).map(s -> s.codePoints().toArray()).toArray(int[][]::new);
        int lines = 1;
        for (int i = 0; i < cells.length; i++) lines = Math.max(lines, (points[i].length + widths[i] - 1) / widths[i]);
        for (int line = 0; line < lines; line++) {
            out.print("|");
            for (int i = 0; i < cells.length; i++) {
                int offset = Math.min(line * widths[i], points[i].length);
                int length = Math.min(widths[i], points[i].length - offset);
                String part = new String(points[i], offset, length);
                String padding = " ".repeat(widths[i] - length);
                out.print(" " + (i == numeric ? padding + part : part + padding) + " |");
            }
            out.println();
        }
    }
    public void history(List<BackupResult> results) {
        out.println("\nRIWAYAT PEKERJAAN");
        table(new String[]{"ID", "Status", "Jenis", "Total KB"}, new int[]{8, 10, 11, 9},
            results.stream().map(r -> new String[]{r.id(), r.status().toString(), r.plan().type(), "" + r.plan().totalKb()}).toList(), 3);
    }
    public void plan(BackupPlan plan) {
        out.println("\nPREVIEW " + plan.type());
        out.println("Baseline : " + (plan.baselineId() == null ? "belum ada" : plan.baselineId()));
        table(new String[]{"Perubahan", "Path", "KB", "Revision"}, new int[]{9, 36, 7, 8},
            plan.entries().stream().map(e -> new String[]{e.change().toString(), e.file().path(), "" + e.file().sizeKb(), "r" + e.file().revision()}).toList(), 2);
        out.println("Dipilih  : " + plan.selectedCount() + " dari " + plan.entries().size() + " berkas");
        out.println("Total    : " + plan.totalKb() + " KB");
    }
    public void result(BackupResult r) {
        out.println("\nHASIL BACKUP");
        out.println("ID pekerjaan  : " + r.id());
        out.println("Status        : " + r.status());
        out.println("Jenis         : " + r.plan().type());
        out.println("Waktu         : " + r.time().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm:ss")));
        out.println("Berkas dipilih: " + r.plan().selectedCount());
        out.println("Total ukuran  : " + r.plan().totalKb() + " KB");
        out.println("Snapshot baru : " + (r.snapshotId() == null ? "tidak dibuat" : r.snapshotId()));
    }
}
