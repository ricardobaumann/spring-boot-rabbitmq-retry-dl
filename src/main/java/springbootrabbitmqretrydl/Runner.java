package springbootrabbitmqretrydl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Runner {

    private final RabbitTemplate rabbitTemplate;
    private final AtomicLong atomicALong = new AtomicLong(1L);

    public Runner(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void run() {
        long id = atomicALong.getAndIncrement();
        System.out.println("Sending message "+id);
        rabbitTemplate.convertAndSend(SpringBootRabbitmqRetryDlApplication.DEFAULT_EXCHANGE, "work", String.valueOf(id));
    }

}
