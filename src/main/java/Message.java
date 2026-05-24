import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Message {

    String date;
    String time;
    String name;
    String message;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Message(String date, String time, String name, String message) {
        this.date = date;
        this.time = time;
        this.name = name;
        this.message = message;
    }

    public Message() {

    }

    public String getDateString() {
        return date;
    }

    public String getTimeString() {
        return time;
    }

    public LocalDate getDate() {
        return LocalDate.parse(date, dateFormatter);
    }

    public LocalTime getTime() {
        return LocalTime.parse(time, timeFormatter);
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String printMessage() {
        return "[" +date +", " +time +"]" +" " +name +": " +message;
    }
}
