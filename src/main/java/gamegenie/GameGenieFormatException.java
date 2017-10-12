package gamegenie;

public class GameGenieFormatException extends Throwable {
    public GameGenieFormatException(String code) {
        super(String.format("Invalid code \"%s\"", code));
    }
}
