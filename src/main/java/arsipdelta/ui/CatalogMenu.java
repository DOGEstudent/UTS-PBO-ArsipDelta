package arsipdelta.ui;

import arsipdelta.model.FileRecord;
import arsipdelta.service.CatalogService;
import arsipdelta.validation.PathValidator;
import java.io.PrintWriter;

public final class CatalogMenu {
    private final CatalogService catalog;
    private final ConsoleInput input;
    private final PrintWriter out;
    public CatalogMenu(CatalogService catalog, ConsoleInput input, PrintWriter out) {
        this.catalog = catalog; this.input = input; this.out = out;
    }
    public void add() {
        if (catalog.files().size() >= CatalogService.LIMIT) throw new IllegalArgumentException("Katalog penuh.");
        out.println("\nTAMBAH BERKAS\nKetik /batal untuk membatalkan.");
        // Validasi awal menjaga retry pada kolom path; add() tetap menjaga invariant service.
        String path = input.ask("Path relatif: ", s -> { catalog.validateNewPath(s); return PathValidator.normalize(s); });
        int size = input.number("Ukuran KB (1-1000000): ", 1, 1_000_000);
        out.println("\nRINGKASAN BERKAS\nPath     : " + path + "\nUkuran   : " + size + " KB\nRevision : 1");
        if (input.confirm("Simpan?")) out.println("Tersimpan: " + catalog.add(path, size).id());
        else out.println("Dibatalkan. Tidak ada perubahan.");
    }
    public void update() {
        if (catalog.files().isEmpty()) throw new IllegalArgumentException("Katalog kosong.");
        out.println("\nPERBARUI BERKAS");
        FileRecord old = input.ask("ID berkas (/batal): ", catalog::find);
        out.println("Path     : " + old.path() + "\nUkuran   : " + old.sizeKb() + " KB\nRevision : " + old.revision());
        int size = input.number("Ukuran revisi baru KB: ", 1, 1_000_000);
        out.println("Isi dianggap berubah, termasuk jika ukuran sama. Revision akan naik satu.");
        if (input.confirm("Catat revisi baru?")) out.println("Diperbarui: r" + catalog.update(old.id(), size).revision());
        else out.println("Dibatalkan. Tidak ada perubahan.");
    }
}
