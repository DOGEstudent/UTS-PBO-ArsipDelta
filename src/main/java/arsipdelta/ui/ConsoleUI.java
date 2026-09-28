package arsipdelta.ui;

import arsipdelta.service.*;
import java.io.*;
import java.util.Comparator;
import arsipdelta.model.FileRecord;

public final class ConsoleUI {
    private final CatalogService catalog = new CatalogService();
    private final BackupService backup = new BackupService(catalog);
    private final ConsoleInput input;
    private final ConsolePrinter printer;
    private final PrintWriter out;
    private final CatalogMenu catalogMenu;
    private final BackupMenu backupMenu;
    public ConsoleUI(Reader reader, PrintWriter out) {
        this.out = out;
        input = new ConsoleInput(reader, out);
        printer = new ConsolePrinter(out, input);
        catalogMenu = new CatalogMenu(catalog, input, out);
        backupMenu = new BackupMenu(backup, input, printer, out);
    }
    public void run() {
        out.println("ARSIPDELTA - SIMULASI BACKUP\nData hanya bertahan selama sesi.");
        try {
            while (true) {
                out.println("\nMENU UTAMA\n----------\nKatalog   : " + catalog.files().size() + "/500 berkas\nPekerjaan : " + backup.history().size() + "/100\n");
                out.println("1. Lihat katalog\n2. Tambah berkas\n3. Perbarui berkas\n4. Rencanakan backup\n5. Snapshot terakhir\n6. Riwayat pekerjaan\n7. Panduan\n0. Keluar");
                try {
                    int choice = input.number("Pilihan: ", 0, 7);
                    switch (choice) {
                        case 0: if (input.confirm("Keluar? Seluruh data sesi akan hilang.")) return; break;
                        case 1: out.println("\nKATALOG BERKAS"); printer.files(catalog.files()); break;
                        case 2: catalogMenu.add(); break;
                        case 3: catalogMenu.update(); break;
                        case 4: backupMenu.run(); break;
                        case 5:
                            out.println("\nSNAPSHOT TERAKHIR");
                            if (backup.baseline() == null) out.println("Belum ada snapshot.");
                            else { out.println("ID snapshot : " + backup.baseline().id()); printer.files(backup.baseline().files().values().stream().sorted(Comparator.comparing(FileRecord::id)).toList()); }
                            break;
                        case 6: backupMenu.history(); break;
                        case 7:
                            out.println("\nPANDUAN SINGKAT");
                            out.println("Tambah berkas -> full -> perbarui/tambah -> incremental.\nRevision menentukan perubahan; ukuran hanya untuk total.\nBaseline = backup terakhir SUCCESS. /batal membatalkan formulir.\nBatas: 500 berkas, 100 pekerjaan, 256 karakter/baris.\nTidak ada filesystem backup, database, jaringan, atau penyimpanan permanen.");
                            break;
                        default: throw new IllegalStateException("Menu tidak terjangkau.");
                    }
                } catch (ConsoleInput.Cancelled e) { out.println("Operasi dibatalkan. Kembali ke menu utama."); }
                  catch (IllegalArgumentException e) { out.println("[!] " + e.getMessage()); }
            }
        } catch (ConsoleInput.EndOfInput e) { out.println("\nInput berakhir. Sesi ditutup."); }
        finally { out.flush(); }
    }
}
