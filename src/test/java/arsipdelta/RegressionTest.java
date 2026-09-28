package arsipdelta;

import arsipdelta.model.*;
import arsipdelta.service.*;
import arsipdelta.ui.*;
import java.io.*;

/** Test mandiri tanpa dependency; AssertionError membuat Maven test gagal. */
public final class RegressionTest {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static void reject(Runnable operation) {
        try { operation.run(); } catch (IllegalArgumentException e) { checks++; return; }
        throw new AssertionError("Operasi invalid diterima");
    }
    public static void main(String[] args) {
        CatalogService c = new CatalogService();
        BackupService b = new BackupService(c);
        reject(() -> b.plan(false));
        reject(() -> c.add("", 1));
        reject(() -> c.add("../escape", 1));
        reject(() -> c.add("C:/absolute", 1));
        reject(() -> c.add("src//x", 1));
        reject(() -> c.add("x\u001b[31m", 1));
        reject(() -> c.add("ok", 0));
        check(c.files().isEmpty(), "Invalid add changed catalog");
        FileRecord a = c.add("src\\Main.java", 10);
        check(a.id().equals("FIL-0001"), "ID spent by invalid add");
        reject(() -> c.add("SRC/main.java", 10));
        reject(() -> b.plan(true));
        BackupPlan full = b.plan(false);
        b.cancel(); reject(() -> b.execute(full));
        check(b.history().isEmpty() && b.baseline() == null, "Cancel changed state");
        BackupResult first = b.execute(b.plan(false));
        check(first.id().equals("JOB-0001"), "Job ID incorrect");
        BackupSnapshot old = b.baseline();
        c.update(a.id(), 10);
        check(old.files().get(a.id()).revision() == 1, "Snapshot mutated");
        check(first.plan().entries().get(0).file().revision() == 1, "History mutated");
        BackupPlan incremental = b.plan(true);
        check(incremental.selectedCount() == 1, "Same size revision missed");
        b.execute(incremental);
        reject(() -> b.execute(incremental));
        check(b.baseline().files().get(a.id()).revision() == 2, "Baseline not advanced");
        String baselineId = b.baseline().id();
        check(b.execute(b.plan(true)).status() == BackupStatus.NO_CHANGES, "Repeat incremental wrong");
        check(b.baseline().id().equals(baselineId), "No changes advanced baseline");
        BackupPlan stale = b.plan(false);
        c.add("README.md", 5);
        int before = b.history().size();
        reject(() -> b.execute(stale));
        check(b.history().size() == before && b.baseline().id().equals(baselineId), "Rejected plan changed state");
        check(b.plan(true).selectedCount() == 1, "New file comparison wrong");
        BackupPlan issued = b.plan(false);
        reject(() -> b.execute(new BackupPlan(issued.type(), issued.catalogVersion(), issued.baselineId(), issued.entries())));
        b.execute(issued);
        try { old.files().clear(); throw new AssertionError("Mutable snapshot"); }
        catch (UnsupportedOperationException expected) { checks++; }
        try { b.history().clear(); throw new AssertionError("Mutable history"); }
        catch (UnsupportedOperationException expected) { checks++; }
        while (c.files().size() < 500) c.add("file" + c.files().size(), 1);
        reject(() -> c.add("overflow", 1));
        while (b.history().size() < 100) b.execute(b.plan(false));
        reject(() -> b.plan(false));
        check(b.history().size() == 100, "History limit wrong");
        inputTests();
        paginationTests();
        System.out.println("PASS: " + checks + " regression checks");
    }
    private static void paginationTests() {
        var rows = java.util.stream.IntStream.rangeClosed(1, 16).mapToObj(i -> "ROW-" + i).toList();
        StringWriter text = new StringWriter();
        PrintWriter out = new PrintWriter(text);
        new ConsolePrinter(out, new ConsoleInput(new StringReader(""), out)).pages(rows.subList(0, 15));
        check(text.toString().contains("ROW-15") && !text.toString().contains("berikutnya"), "15 rows prompted unnecessarily");
        for (String answer : new String[]{"y\n", "n\n"}) {
            text = new StringWriter(); out = new PrintWriter(text);
            new ConsolePrinter(out, new ConsoleInput(new StringReader(answer), out)).pages(rows);
            check(text.toString().contains("ROW-16") == answer.startsWith("y"), "Pagination choice ignored");
        }
        for (String answer : new String[]{"/batal\n", ""}) {
            CatalogService catalog = new CatalogService();
            for (int i = 0; i < 16; i++) catalog.add("file" + i, 1);
            BackupService service = new BackupService(catalog);
            BackupSnapshot baseline = service.execute(service.plan(false)).snapshotId() == null ? null : service.baseline();
            catalog.update("FIL-0001", 2);
            text = new StringWriter(); out = new PrintWriter(text);
            ConsoleInput input = new ConsoleInput(new StringReader("2\n" + answer), out);
            try {
                new BackupMenu(service, input, new ConsolePrinter(out, input), out).run();
                throw new AssertionError("Pagination did not interrupt preview");
            } catch (ConsoleInput.Cancelled e) { check(!answer.isEmpty(), "Wrong cancellation type"); }
              catch (ConsoleInput.EndOfInput e) { check(answer.isEmpty(), "Wrong EOF type"); }
            check(service.history().size() == 1 && service.baseline() == baseline, "Interrupted preview mutated state");
            check(service.execute(service.plan(true)).id().equals("JOB-0002"), "Interrupted preview spent job ID");
        }
        String script = "";
        for (int i = 0; i < 16; i++) script += "2\nf" + i + "\n1\ny\n";
        text = new StringWriter();
        new ConsoleUI(new StringReader(script + "1\n/batal\n0\ny\n"), new PrintWriter(text)).run();
        check(text.toString().contains("Operasi dibatalkan. Kembali ke menu utama."), "Missing neutral cancellation message");
    }
    private static void inputTests() {
        StringWriter output = new StringWriter();
        ConsoleInput input = new ConsoleInput(new StringReader("\nabc\n999999999999999999\n0\n" + "x".repeat(10000) + "\n42\r\n"), new PrintWriter(output));
        check(input.number("Size: ", 1, 100) == 42, "Recovery failed");
        check(output.toString().contains("maks. 256"), "Long input not rejected");
        input = new ConsoleInput(new StringReader("invalid\n".repeat(5000) + "7\n"), new PrintWriter(Writer.nullWriter()));
        check(input.number("", 0, 7) == 7, "Repeated invalid input failed");
        input = new ConsoleInput(new StringReader("/batal\n"), new PrintWriter(Writer.nullWriter()));
        try { input.number("", 1, 10); throw new AssertionError("Cancel ignored"); }
        catch (ConsoleInput.Cancelled expected) { checks++; }
        input = new ConsoleInput(new StringReader("unfinished"), new PrintWriter(Writer.nullWriter()));
        try { input.number("", 1, 10); throw new AssertionError("EOF ignored"); }
        catch (ConsoleInput.EndOfInput expected) { checks++; }
        StringWriter uiOutput = new StringWriter();
        new ConsoleUI(new StringReader("2\nsrc/A.java\n/batal\n1\n0\ny\n"), new PrintWriter(uiOutput)).run();
        check(uiOutput.toString().contains("Belum ada data."), "Form cancellation committed data");
        uiOutput = new StringWriter();
        new ConsoleUI(new StringReader("2\n"), new PrintWriter(uiOutput)).run();
        check(uiOutput.toString().contains("Input berakhir"), "EOF exit failed");
    }
}
