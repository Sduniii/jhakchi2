package fel;

public class FelException extends Exception {
    public FelException(Object obj){
        super(obj.toString());
    }
}
