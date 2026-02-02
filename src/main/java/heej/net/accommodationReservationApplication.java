package heej.net;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
public class accommodationReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(heej.net.accommodationReservationApplication.class, args);
    }

}
