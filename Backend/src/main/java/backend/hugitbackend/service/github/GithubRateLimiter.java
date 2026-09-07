package backend.hugitbackend.service.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component

public class GithubRateLimiter {

    private final long delayMs;

    public GithubRateLimiter(@Value("${api.github.api-delay-ms:50}") long delay){
        this.delayMs=Math.max(0,delay);
    }

    public void pause(){
        if(delayMs<=0){
            return;
        }

        try {
            Thread.sleep(delayMs);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted While Rate Limiting"+e.getMessage());
        }
    }

}
