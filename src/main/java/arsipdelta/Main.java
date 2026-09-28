package arsipdelta;

import arsipdelta.ui.ConsoleUI;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        PrintWriter out = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        try { new ConsoleUI(new InputStreamReader(System.in, StandardCharsets.UTF_8), out).run(); }
        catch (UncheckedIOException e) { System.err.println("Input/output gagal. Sesi dihentikan; data tidak disimpan."); System.exit(1); }
    }
}
