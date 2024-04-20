package catx.feitu.CozeProxy.exception;

import java.util.ArrayList;
import java.util.List;

public class RunningFailedException extends Exception {
    public final List<Exception> exceptions;
    public final String key;
    public RunningFailedException(List<Exception> exceptions_,String key_) {
        super();
        exceptions = exceptions_;
        key = key_;
    }
}
