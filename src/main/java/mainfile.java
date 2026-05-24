import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import java.io.File;

enum State {
    NEW_ENTRY,
    CONTINUATION
}

public class mainfile {

    static String PERSON1 = "NAME_OF_CHAT_PARTNER";     //Make sure you put your partners name exactly how it appears in chat
    static String PERSON2 = "YOUR_NAME";                //Make sure you put your name exactly how it appears in chat
    static String KEYWORD = "KEYWORD_TO_COUNT";         //Enter any word that you want counted like "hello" or "pizza"
    static int TOP_WORDS_LIMIT = 15;                    //Set limit for how many of the most frequently used words are being displayed (15)
    static int TOP_WORDS_USER_LIMIT = 10;               //Set limit for how many of the most frequently used words per user are being displayed (10)
    static String TITLE = "OUR_WHATSAPP_CHAT";          //Set title of your summary
    static String FILENAME = "_chat.txt";               //Name of file containing chatlogs (_chat.txt)
    static String MAINPATHCHAT = "PATH_TO_YOUR_FOLDER";     //Path to folder containing chatlogs and media files (don't forget '\' at the end!)
    static String OUTPATH = "PATH_TO_YOUR_OUTPUT_FILE";      //Path for your output file (don't forget '\' at the end!)
    static List<String> MEDIA = Arrays.asList("AUDIO", "GIF", "PHOTO", "STICKER", "VIDEO");         //leave unchanged

    private static boolean isNewEntry(String line) {
        return line.matches("^\\[\\d{2}\\.\\d{2}\\.\\d{2}, \\d{2}:\\d{2}:\\d{2}].*");
    }

    private static State classify(String line) {
        return isNewEntry(line) ? State.NEW_ENTRY : State.CONTINUATION;
    }

    private static Message parse(String line) {
        int closeBracket = line.indexOf(']');
        if (closeBracket == -1) return null;

        String header = line.substring(1, closeBracket);
        String[] dateTime = header.split(", ");
        if (dateTime.length < 2) return null;

        String rest = line.substring(closeBracket + 2);

        int colonSpace = rest.indexOf(": ");
        String name;
        String message;

        if (colonSpace != -1) {
            name = rest.substring(0, colonSpace).trim();
            message = rest.substring(colonSpace + 2);
        } else {
            int colon = rest.indexOf(':');
            if (colon != -1) {
                name = rest.substring(0, colon).trim();
                message = rest.substring(colon + 1).trim();
            } else {
                return null;
            }
        }

        Message msg = new Message();
        msg.date = dateTime[0];
        msg.time = dateTime[1];
        msg.name = name;
        msg.message = message;

        return msg;
    }

    private static List<Message> save_all_messages(String main_path, String filename) throws IOException {
        List<Message> all_messages = new ArrayList<>();
        Message current = null;


        try (BufferedReader br = Files.newBufferedReader(Path.of(main_path + filename), StandardCharsets.UTF_8)) {
            String rawLine;
            while ((rawLine = br.readLine()) != null) {

                String line = rawLine.replaceFirst("^[\\uFEFF\\u200E\\u200F\\u202A-\\u202E]+", "");
                line = line.stripLeading();

                State state = classify(line);

                if (state == State.NEW_ENTRY) {
                    if (current != null && !current.message.isBlank()) {
                        all_messages.add(current);
                        //System.out.println(current.message);
                    }
                    current = parse(line);
                } else {
                    if (current != null) {
                        current.message += "\n" + line;
                    }
                }
            }

            if (current != null && !current.message.isBlank()) {
                all_messages.add(current);
            }
        }
        return all_messages;
    }

    private static Map<String, Long> count_occurences(Set<String> all) {
        return all.stream()
                .map(s -> s.split("-"))
                .filter(parts -> parts.length >= 2)
                .map(parts -> parts[1])
                .filter(s -> MEDIA.contains(s))
                .collect(groupingBy(
                        Function.identity(),
                        counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static Map<String, Long> count_messages_per_user(List<Message> all_messages) {
        return all_messages.stream()
                .map(Message::getName)
                .collect(groupingBy(
                        Function.identity(),
                        counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static Map<String, Long> get_top_words(List<Message> all_messages, int limit) {
        return all_messages.stream()
                .map(Message::getMessage)
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .map(String::toLowerCase)
                .filter(s -> !s.contains("<attached:"))
                .collect(groupingBy(
                        Function.identity(),
                        counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static Map<String, Long> get_top_words_from_user(List<Message> all_messages, int limit, String username) {
        return all_messages.stream()
                .filter(s -> s.getName().equals(username))
                .map(Message::getMessage)
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .map(String::toLowerCase)
                .filter(s -> !s.contains("<attached:"))
                .collect(groupingBy(
                        Function.identity(),
                        counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static long count_word_from_user(List<Message> all_messages, String username, String keyword) {
        return all_messages.stream()
                .filter(s -> s.getName().equals(username))
                .map(Message::getMessage)
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .filter(s -> s.equalsIgnoreCase(keyword))
                .count();
    }

    private static long count_total_words_from_user(List<Message> all_messages, String username) {
        return all_messages.stream()
                .filter(s -> s.getName().equals(username))
                .map(Message::getMessage)
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .count();
    }

    private static MessageGap get_biggest_gap(List<Message> all_messages) {
        return IntStream
                .range(0, all_messages.size() - 1)
                .mapToObj(i -> {
                    Message first = all_messages.get(i);
                    Message second = all_messages.get(i + 1);

                    LocalDateTime firstDateTime = LocalDateTime.of(first.getDate(), first.getTime());
                    LocalDateTime secondDateTime = LocalDateTime.of(second.getDate(), second.getTime());

                    Duration gap = Duration.between(firstDateTime, secondDateTime);
                    return new MessageGap(first, second, gap);
                })
                .filter(s -> !s.first().getName().equals(s.second().getName()))
                .max(Comparator.comparing(MessageGap::gap))
                .orElseThrow();
    }

    private static MessageGap get_smallest_gap(List<Message> all_messages) {
        return IntStream
                .range(0, all_messages.size() - 1)
                .mapToObj(i -> {
                    Message first = all_messages.get(i);
                    Message second = all_messages.get(i + 1);

                    LocalDateTime firstDateTime = LocalDateTime.of(first.getDate(), first.getTime());
                    LocalDateTime secondDateTime = LocalDateTime.of(second.getDate(), second.getTime());

                    Duration gap = Duration.between(firstDateTime, secondDateTime);
                    return new MessageGap(first, second, gap);
                })
                .filter(s -> !s.first().getName().equals(s.second().getName()))
                .filter(s -> s.gap().isPositive())
                .min(Comparator.comparing(MessageGap::gap))
                .orElseThrow();
    }

    private static String gap_time_formatted(MessageGap msg_gap) {
        Duration gap = msg_gap.gap();
        return gap.toDays() +" days, " +gap.toHoursPart() +" hours, " +gap.toMinutesPart() +" minutes, " +gap.toSecondsPart() +" seconds";
    }

    private static OptionalDouble get_total_average_time(List<Message> all_messages) {
        return IntStream
                .range(0, all_messages.size() - 1)
                .mapToObj(i -> {
                    Message first = all_messages.get(i);
                    Message second = all_messages.get(i + 1);

                    LocalDateTime firstDateTime = LocalDateTime.of(first.getDate(), first.getTime());
                    LocalDateTime secondDateTime = LocalDateTime.of(second.getDate(), second.getTime());

                    Duration gap = Duration.between(firstDateTime, secondDateTime);
                    return new MessageGap(first, second, gap);
                })
                .filter(s -> s.first().getDate().equals(s.second().getDate()))
                .filter(s -> !s.first().getName().equals(s.second().getName()))
                .filter(s -> s.gap().isPositive())
                .mapToLong(s -> s.gap().toMinutes())
                .average();
    }

    private static OptionalDouble get_user_average_time(List<Message> all_messages, String username) {
        return IntStream
                .range(0, all_messages.size() - 1)
                .mapToObj(i -> {
                    Message first = all_messages.get(i);
                    Message second = all_messages.get(i + 1);

                    LocalDateTime firstDateTime = LocalDateTime.of(first.getDate(), first.getTime());
                    LocalDateTime secondDateTime = LocalDateTime.of(second.getDate(), second.getTime());

                    Duration gap = Duration.between(firstDateTime, secondDateTime);
                    return new MessageGap(first, second, gap);
                })
                .filter(s -> s.first().getDate().equals(s.second().getDate()))
                .filter(s -> !s.first().getName().equals(s.second().getName()))
                .filter(s -> s.second().getName().equals(username))
                .filter(s -> s.gap().isPositive())
                .mapToLong(s -> s.gap().toMinutes())
                .average();
    }

    private static Map.Entry<String, Long> get_most_msgs_on_day(List<Message> all_messages) {
        return all_messages.stream()
                .map(Message::getDateString)
                .collect(groupingBy(
                        Function.identity(),
                        counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get();
    }

    private static Map<Integer, Long> count_messages_by_hour(List<Message> all_messages) {
        return all_messages.stream()
                .collect(groupingBy(m -> m.getTime().getHour(), counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private static Map<DayOfWeek, Long> count_messages_by_weekday(List<Message> all_messages) {
        return all_messages.stream()
                .collect(groupingBy(m -> m.getDate().getDayOfWeek(), counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    public static void main(String[] args) throws IOException {
        Set<String> all_files = Stream.of(Objects.requireNonNull(new File(MAINPATHCHAT).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(toSet());

        ArrayList<Long> special_word_count = new ArrayList<>();
        ArrayList<Long> total_words_from_users = new ArrayList<>();
        ArrayList<ChatStats.TopEmoji> topEmojis = new ArrayList<>();
        ArrayList<ChatStats.TimeSlot> hours_morning = new ArrayList<>();
        ArrayList<ChatStats.MediaCount> media_counts = new ArrayList<>();
        ArrayList<ChatStats.TimeSlot> hours_afternoon = new ArrayList<>();
        ArrayList<ChatStats.DayOfWeek> records_weekdays = new ArrayList<>();
        ArrayList<ChatStats.MostMessaged> msgs_per_user = new ArrayList<>();
        ArrayList<Map<String, Long>> top_words_from_users = new ArrayList<>();
        ArrayList<ChatStats.MostUsedWords> most_used_words = new ArrayList<>();
        ArrayList<ChatStats.WordCount> special_word_count_per_user = new ArrayList<>();
        ArrayList<ChatStats.ParticipantWordStats> participantWordStats = new ArrayList<>();

        Map<String, Long> media_occurences = count_occurences(all_files);
        List<Message> all_messages = save_all_messages(MAINPATHCHAT, FILENAME);
        Map<String, Long> top_words = get_top_words(all_messages, TOP_WORDS_LIMIT);
        Map<String, Long> messages_per_user = count_messages_per_user(all_messages);
        special_word_count.add(count_word_from_user(all_messages, PERSON1, KEYWORD));
        special_word_count.add(count_word_from_user(all_messages, PERSON2, KEYWORD));
        MessageGap biggest_gap = get_biggest_gap(all_messages);
        MessageGap smallest_gap = get_smallest_gap(all_messages);
        OptionalDouble average_time = get_total_average_time(all_messages);
        OptionalDouble average_time_person1 = get_user_average_time(all_messages, PERSON1);
        OptionalDouble average_time_person2 = get_user_average_time(all_messages, PERSON2);
        Map.Entry<String, Long> most_msgs = get_most_msgs_on_day(all_messages);
        Map<String, Long> top_emojis = EmojiCounter.topEmojis(all_messages);
        Map<Integer, Long> messages_by_hour = count_messages_by_hour(all_messages);
        Map<DayOfWeek, Long> messages_by_weekday = count_messages_by_weekday(all_messages);

        ChatStats.ChatDates chatDates = new ChatStats.ChatDates();
        chatDates.start = all_messages.getFirst().getDateString();
        chatDates.end = all_messages.getLast().getDateString();

        media_occurences.forEach((key, value) -> {
            ChatStats.MediaCount tmp = new ChatStats.MediaCount();
            tmp.name = key;
            tmp.count = value;
            media_counts.add(tmp);
        });

        top_emojis.forEach((key, value) -> {
            ChatStats.TopEmoji tmp = new ChatStats.TopEmoji();
            tmp.emoji = key;
            tmp.value = value;
            topEmojis.add(tmp);
        });

        top_words.forEach((key, value) -> {
            ChatStats.MostUsedWords tmp = new ChatStats.MostUsedWords();
            tmp.word = key;
            tmp.value = value;
            most_used_words.add(tmp);
        });

        for (int i = 0; i<12; i++) {
            ChatStats.TimeSlot tmp = new ChatStats.TimeSlot();
            tmp.hour = i +":00";
            tmp.count = messages_by_hour.get(i);
            hours_morning.add(tmp);
        }

        for (int i = 12; i<24; i++) {
            ChatStats.TimeSlot tmp = new ChatStats.TimeSlot();
            tmp.hour = i +":00";
            tmp.count = messages_by_hour.get(i);
            hours_afternoon.add(tmp);
        }

        messages_by_weekday.forEach((key, value) -> {
            ChatStats.DayOfWeek tmp = new ChatStats.DayOfWeek();
            tmp.day = key.name();
            tmp.count = value;
            records_weekdays.add(tmp);
        });

        List<ChatStats.Record> records = new ArrayList<>();
        ChatStats.Record r1 = new ChatStats.Record();
        ChatStats.Record r2 = new ChatStats.Record();
        ChatStats.Record r3 = new ChatStats.Record();
        r1.label = "Most messages in one day (" +most_msgs.getKey() +")";
        r1.value = String.valueOf(most_msgs.getValue());
        r2.label = "Longest time apart";
        r2.value = gap_time_formatted(biggest_gap);
        r3.label = "Fastest reply";
        r3.value = gap_time_formatted(smallest_gap);
        records.add(r1);
        records.add(r2);
        records.add(r3);

        messages_per_user.forEach((key, value) -> {
            ChatStats.MostMessaged tmp = new ChatStats.MostMessaged();
            tmp.name = key;
            tmp.count = value;
            msgs_per_user.add(tmp);
        });

        ChatStats.WordCount p1 = new ChatStats.WordCount();
        ChatStats.WordCount p2 = new ChatStats.WordCount();
        p1.name = PERSON1;
        p1.word = KEYWORD;
        p1.count = special_word_count.get(0);
        p2.name = PERSON2;
        p2.word = KEYWORD;
        p2.count = special_word_count.get(1);
        special_word_count_per_user.add(p1);
        special_word_count_per_user.add(p2);


        total_words_from_users.add(count_total_words_from_user(all_messages, PERSON1));
        total_words_from_users.add(count_total_words_from_user(all_messages, PERSON2));
        top_words_from_users.add(get_top_words_from_user(all_messages, TOP_WORDS_USER_LIMIT, PERSON1));
        top_words_from_users.add(get_top_words_from_user(all_messages, TOP_WORDS_USER_LIMIT, PERSON2));

        ChatStats.ParticipantWordStats pws1 = new ChatStats.ParticipantWordStats();
        pws1.name = PERSON1;
        pws1.totalWords = total_words_from_users.get(0);
        pws1.topWords = top_words_from_users.get(0).entrySet().stream()
                .map(e -> { ChatStats.ParticipantWordStats.WordCount wc = new ChatStats.ParticipantWordStats.WordCount(); wc.word = e.getKey(); wc.count = e.getValue(); return wc; })
                .collect(Collectors.toList());

        ChatStats.ParticipantWordStats pws2 = new ChatStats.ParticipantWordStats();
        pws2.name = PERSON2;
        pws2.totalWords = total_words_from_users.get(1);
        pws2.topWords = top_words_from_users.get(1).entrySet().stream()
                .map(e -> { ChatStats.ParticipantWordStats.WordCount wc = new ChatStats.ParticipantWordStats.WordCount(); wc.word = e.getKey(); wc.count = e.getValue(); return wc; })
                .collect(Collectors.toList());

        participantWordStats.add(pws1);
        participantWordStats.add(pws2);



        ChatStats stats = new ChatStats();

        stats.title = TITLE;
        stats.chatDates = chatDates;
        stats.participants = new ChatStats.Participants();
        stats.participants.left = PERSON1;
        stats.participants.right = PERSON2;
        stats.replyTimes = new ChatStats.ReplyTimes();
        stats.replyTimes.left = (int) average_time_person1.getAsDouble();
        stats.replyTimes.right = (int) average_time_person2.getAsDouble();
        stats.topEmojis = topEmojis;
        stats.mostUsedWords = most_used_words;
        stats.participantWordStats = participantWordStats;
        stats.records = records;
        stats.timeSlotsMorning = hours_morning;
        stats.timeSlotsAfternoon = hours_afternoon;
        stats.daysOfWeek = records_weekdays;
        stats.mostMessaged = msgs_per_user;
        stats.wordCounts = special_word_count_per_user;
        stats.mediaCounts = media_counts;

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(OUTPATH +"stats.json"), stats);

    }
}
