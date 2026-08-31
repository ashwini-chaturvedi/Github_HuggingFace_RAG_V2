package backend.devpilotbackend.service.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
