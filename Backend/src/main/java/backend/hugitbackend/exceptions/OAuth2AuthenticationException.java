package backend.hugitbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OAuth2AuthenticationException extends RuntimeException{

    public OAuth2AuthenticationException(String msg){
        super(msg);
    }
}
