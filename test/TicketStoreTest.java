import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ТЕСТИРОВЩИК: простые автотесты без внешних библиотек.
 * Запуск: см. run-tests.sh / run-tests.bat
 */
public class TicketStoreTest {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  OK    " + name); }
        else    { failed++; System.out.println("  FAIL  " + name); }
    }

    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("helpdesk-test", ".txt");
        Files.delete(file);

        TicketStore store = new TicketStore(file);
        check("новое хранилище пустое", store.all().isEmpty());

        Ticket t = store.add("Не работает принтер", "Строка 1\nСтрока 2\tс табом", "Анна");
        check("первая заявка получает id = 1", t.id == 1);
        check("новая заявка имеет статус NEW", Ticket.NEW.equals(t.status));

        check("смена статуса на IN_PROGRESS проходит", store.setStatus(1, Ticket.IN_PROGRESS));
        check("неверный статус отклоняется", !store.setStatus(1, "WRONG"));
        check("несуществующая заявка отклоняется", !store.setStatus(99, Ticket.DONE));

        TicketStore reloaded = new TicketStore(file);
        check("после перезапуска заявка сохранилась", reloaded.all().size() == 1);
        Ticket r = reloaded.all().get(0);
        check("статус сохранился", Ticket.IN_PROGRESS.equals(r.status));
        check("перенос строки и таб в описании сохранились", "Строка 1\nСтрока 2\tс табом".equals(r.description));
        check("кириллица сохранилась", "Анна".equals(r.author));
        check("следующий id продолжает нумерацию", reloaded.add("Вторая", "", "Олег").id == 2);

        Files.deleteIfExists(file);
        System.out.println("\nПройдено: " + passed + ", провалено: " + failed);
        if (failed > 0) System.exit(1);
    }
}
