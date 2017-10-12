package gamegenie;

public class GameGenieNotFoundException extends Throwable {
    public GameGenieNotFoundException(String code) {
        super(String.format("Invalid code \"%s\"", code));
    }
}
