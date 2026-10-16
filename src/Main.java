import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BACKEND: веб-сервер хелпдеска. Только стандартная библиотека Java (Java 11+).
 *
 *   GET  /api/tickets              - список заявок (JSON)
 *   POST /api/tickets              - создать заявку (title, description, author)
 *   POST /api/tickets/{id}/status  - сменить статус (status=NEW|IN_PROGRESS|DONE)
 *   всё остальное                  - файлы из папки web/ (сайт)
 */
public class Main {
    static TicketStore store;
    static Path webDir;

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        store = new TicketStore(Paths.get(System.getenv().getOrDefault("DATA_FILE", "tickets.txt")));
        webDir = Paths.get(System.getenv().getOrDefault("WEB_DIR", "web")).toAbsolutePath().normalize();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/tickets", Main::handleApi);
        server.createContext("/", Main::handleStatic);
        server.start();
        System.out.println("Helpdesk запущен: http://localhost:" + port);
    }

    // ---------- API ----------

    static void handleApi(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (path.equals("/api/tickets") && method.equals("GET")) {
                send(ex, 200, "application/json; charset=utf-8", ticketsToJson(store.all()));

            } else if (path.equals("/api/tickets") && method.equals("POST")) {
                Map<String, String> f = readForm(ex);
                String title = f.getOrDefault("title", "").trim();
                String author = f.getOrDefault("author", "").trim();
                String description = f.getOrDefault("description", "").trim();
                if (title.isEmpty() || author.isEmpty()) {
                    send(ex, 400, "application/json; charset=utf-8", "{\"error\":\"Укажите имя и тему заявки\"}");
                    return;
                }
                Ticket t = store.add(title, description, author);
                send(ex, 201, "application/json; charset=utf-8", ticketToJson(t));

            } else if (path.matches("/api/tickets/\\d+/status") && method.equals("POST")) {
                int id = Integer.parseInt(path.split("/")[3]);
                String status = readForm(ex).getOrDefault("status", "");
                if (store.setStatus(id, status)) {
                    send(ex, 200, "application/json; charset=utf-8", "{\"ok\":true}");
                } else {
                    send(ex, 404, "application/json; charset=utf-8", "{\"error\":\"Заявка или статус не найдены\"}");
                }

            } else {
                send(ex, 404, "application/json; charset=utf-8", "{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            send(ex, 500, "application/json; charset=utf-8", "{\"error\":\"Ошибка сервера\"}");
        }
    }

    // ---------- Статические файлы (сайт) ----------

    static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        Path file = webDir.resolve(path.substring(1)).normalize();
        // защита от выхода за пределы папки web
        if (!file.startsWith(webDir) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain; charset=utf-8", "404 Not found");
            return;
        }
        String name = file.getFileName().toString();
        String type = name.endsWith(".html") ? "text/html; charset=utf-8"
                : name.endsWith(".css") ? "text/css; charset=utf-8"
                : name.endsWith(".js") ? "application/javascript; charset=utf-8"
                : "application/octet-stream";
        sendBytes(ex, 200, type, Files.readAllBytes(file));
    }

    // ---------- Вспомогательные методы ----------

    static Map<String, String> readForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }

    static String jsonEsc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c < 0x20 ? ' ' : c);
            }
        }
        return sb.toString();
    }

    static String ticketToJson(Ticket t) {
        return "{\"id\":" + t.id
                + ",\"title\":\"" + jsonEsc(t.title) + "\""
                + ",\"description\":\"" + jsonEsc(t.description) + "\""
                + ",\"author\":\"" + jsonEsc(t.author) + "\""
                + ",\"status\":\"" + t.status + "\""
                + ",\"createdAt\":\"" + t.createdAt + "\"}";
    }

    static String ticketsToJson(List<Ticket> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ticketToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        sendBytes(ex, code, type, body.getBytes(StandardCharsets.UTF_8));
    }

    static void sendBytes(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
