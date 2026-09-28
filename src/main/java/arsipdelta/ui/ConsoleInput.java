package arsipdelta.ui;

import java.io.*;
import java.util.function.Function;

/** Baris panjang dibuang tanpa ditampung seluruhnya dalam memori. */
public final class ConsoleInput {
    public static final class EndOfInput extends RuntimeException { }
    public static final class Cancelled extends RuntimeException { }
    private final Reader reader;
    private final PrintWriter out;
    private boolean skipLf;
    public ConsoleInput(Reader reader, PrintWriter out) { this.reader = reader; this.out = out; }
    private String line() {
        StringBuilder b = new StringBuilder();
        boolean invalid = false;
        try {
            while (true) {
                int c = reader.read();
                if (c == -1) throw new EndOfInput();
                if (skipLf) { skipLf = false; if (c == '\n') continue; }
                if (c == '\r' || c == '\n') { skipLf = c == '\r'; break; }
                if (b.length() >= 256 || Character.isISOControl(c) || Character.getType(c) == Character.FORMAT)
                    invalid = true;
                else b.append((char)c);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        if (invalid) throw new IllegalArgumentException("Input terlalu panjang (maks. 256) atau mengandung karakter kontrol.");
        return b.toString().trim();
    }
    public <T> T ask(String prompt, Function<String,T> parse) {
        int failures = 0;
        while (true) {
            out.print(prompt); out.flush();
            try {
                String value = line();
                if (value.equalsIgnoreCase("/batal")) throw new Cancelled();
                if (value.isEmpty()) throw new IllegalArgumentException("Input tidak boleh kosong.");
                return parse.apply(value);
            } catch (IllegalArgumentException e) {
                out.println("[!] " + e.getMessage());
                if (++failures % 5 == 0) out.println("Ketik /batal untuk membatalkan. Input valid sebelumnya tetap dipertahankan.");
            }
        }
    }
    public int number(String prompt, int min, int max) {
        return ask(prompt, s -> {
            if (!s.matches("[0-9]{1,10}")) throw new IllegalArgumentException("Masukkan bilangan bulat dalam rentang " + min + "-" + max + ".");
            long n = Long.parseLong(s);
            if (n < min || n > max) throw new IllegalArgumentException("Nilai harus " + min + "-" + max + ".");
            return (int)n;
        });
    }
    public boolean confirm(String prompt) {
        return ask(prompt + " [y/n]: ", s -> {
            if (s.equalsIgnoreCase("y")) return true;
            if (s.equalsIgnoreCase("n")) return false;
            throw new IllegalArgumentException("Pilih y atau n.");
        });
    }
}
