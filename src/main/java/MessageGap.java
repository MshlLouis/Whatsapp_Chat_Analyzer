import java.time.Duration;

record MessageGap(Message first, Message second, Duration gap) {}