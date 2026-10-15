import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BACKEND: хранилище заявок. Хранит всё в обычном текстовом файле (без базы данных),
 * одна заявка = одна строка, поля разделены табом.
 */
public class TicketStore {
    private final Path file;
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 1;

    public TicketStore(Path file) {
        this.file = file;
        load();
    }

    public synchronized List<Ticket> all() {
        return new ArrayList<>(tickets);
    }

    public synchronized Ticket add(String title, String description, String author) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Ticket t = new Ticket(nextId++, title, description, author, Ticket.NEW, time);
        tickets.add(t);
        save();
        return t;
    }

    /** Возвращает true, если заявка найдена и статус изменён. */
    public synchronized boolean setStatus(int id, String status) {
        if (!Ticket.isValidStatus(status)) return false;
        for (Ticket t : tickets) {
            if (t.id == id) {
                t.status = status;
                save();
                return true;
            }
        }
        return false;
    }

    // ---------- работа с файлом ----------

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "");
    }

    private static String unesc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 't' ? '\t' : n == 'n' ? '\n' : n);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\t", -1);
                if (p.length < 6) continue;
                int id = Integer.parseInt(p[0]);
                tickets.add(new Ticket(id, unesc(p[4]), unesc(p[5]), unesc(p[3]), p[1], p[2]));
                if (id >= nextId) nextId = id + 1;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Не удалось прочитать " + file + ": " + e.getMessage());
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(t.id + "\t" + t.status + "\t" + t.createdAt + "\t"
                    + esc(t.author) + "\t" + esc(t.title) + "\t" + esc(t.description));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить " + file + ": " + e.getMessage());
        }
    }
}
