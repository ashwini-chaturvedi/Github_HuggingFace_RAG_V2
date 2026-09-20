package backend.hugitbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class NoEmailFromProviderException extends RuntimeException {
    public NoEmailFromProviderException(String msg){
        super(msg);
    }
}
