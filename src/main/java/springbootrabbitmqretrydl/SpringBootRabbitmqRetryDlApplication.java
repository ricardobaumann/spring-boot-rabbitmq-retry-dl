package springbootrabbitmqretrydl;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class SpringBootRabbitmqRetryDlApplication {

    public static final String DEFAULT_EXCHANGE = "default_exchange";
    private static final String DEAD_LETTER_QUEUE = "dlq";
    private static final String WORKORDER_QUEUE = "work";

    @Bean
    TopicExchange exchange()
    {
        return new TopicExchange(DEFAULT_EXCHANGE);
    }

    @Bean
    Queue deadLetterQueue()
    {
        return new Queue(DEAD_LETTER_QUEUE,true);
    }

    @Bean
    Queue queue()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEFAULT_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUE);
        return new Queue(WORKORDER_QUEUE,true,false,false,args);
    }
    @Bean
    Binding binding(Queue queue, TopicExchange exchange)
    {
        return BindingBuilder.bind(queue).to(exchange).with(WORKORDER_QUEUE);
    }

    @Bean
    Binding bindingDeadLetter(Queue deadLetterQueue, TopicExchange exchange)
    {
        return BindingBuilder.bind(deadLetterQueue).to(exchange).with(DEAD_LETTER_QUEUE);
    }

    @Bean
    RetryOperationsInterceptor interceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(100, 3, 60000)
                .recoverer((message, cause) -> {
                    throw new AmqpRejectAndDontRequeueException(String.format("Message id %s failed after retry", new String(message.getBody())));
                })
                .build();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(WORKORDER_QUEUE);
        container.setMessageListener(listenerAdapter);
        container.setAdviceChain(interceptor());
        return container;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRabbitmqRetryDlApplication.class, args);
    }
}
