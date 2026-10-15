/**
 * BACKEND: модель заявки (тикета).
 */
public class Ticket {
    public static final String NEW = "NEW";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String DONE = "DONE";

    public int id;
    public String title;
    public String description;
    public String author;
    public String status;
    public String createdAt;

    public Ticket(int id, String title, String description, String author, String status, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static boolean isValidStatus(String s) {
        return NEW.equals(s) || IN_PROGRESS.equals(s) || DONE.equals(s);
    }
}
