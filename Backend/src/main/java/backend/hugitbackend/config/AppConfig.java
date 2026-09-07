package backend.hugitbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AppConfig {

    private final RestClient.Builder restClientBuilder;

    //We are creating a ThreadPool which will have workers that will do Indexing Asynchronously in Background
    //Indexing Service will need these workers
    @Bean(name = "indexingExecutor")
    public Executor indexingExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("index-");
        executor.initialize();

        return executor;
    }

}
