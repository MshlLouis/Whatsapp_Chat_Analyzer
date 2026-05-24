import java.util.List;

public class ChatStats {

    public String title;

    public ChatDates chatDates;

    public Participants participants;

    public ReplyTimes replyTimes;

    public List<TopEmoji> topEmojis;

    public List<MostUsedWords> mostUsedWords;

    public List<ParticipantWordStats> participantWordStats;

    public List<Record> records;

    public List<TimeSlot> timeSlotsMorning;

    public List<TimeSlot> timeSlotsAfternoon;

    public List<DayOfWeek> daysOfWeek;

    public List<MostMessaged> mostMessaged;

    public List<WordCount> wordCounts;

    public List<MediaCount> mediaCounts;

    // ---------- nested classes ----------

    public static class ChatDates {
        public String start;
        public String end;
    }

    public static class Participants {
        public String left;
        public String right;
    }

    public static class ReplyTimes {
        public int left;
        public int right;
    }

    public static class TopEmoji {
        public String emoji;
        public Long value;
    }

    public static class MostUsedWords {
        public String word;
        public Long value;
    }

    public static class ParticipantWordStats {
        public String name;
        public Long totalWords;
        public List<WordCount> topWords;

        public static class WordCount {
            public String word;
            public Long count;
        }
    }

    public static class Record {
        public String label;
        public String value;
    }

    public static class TimeSlot {
        public String hour;
        public Long count;
    }

    public static class DayOfWeek {
        public String day;
        public Long count;
    }

    public static class MostMessaged {
        public String name;
        public Long count;
    }

    public static class WordCount {
        public String name;
        public String word;
        public Long count;
    }
    public static class MediaCount {
        public String name;
        public Long count;
    }
}