import java.text.BreakIterator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EmojiCounter {

    public static Map<String, Long> topEmojis(List<Message> messages) {
        return messages.stream()
                .map(Message::getMessage)
                .flatMap(text -> extractGraphemes(text).stream())
                .filter(EmojiCounter::isEmoji)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private static List<String> extractGraphemes(String text) {
        List<String> result = new ArrayList<>();
        BreakIterator it = BreakIterator.getCharacterInstance(Locale.ROOT);
        it.setText(text);

        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            result.add(text.substring(start, end));
        }
        return result;
    }

    private static boolean isEmoji(String s) {
        return s.codePoints().anyMatch(
                cp -> Character.getType(cp) == Character.OTHER_SYMBOL
        );
    }
}