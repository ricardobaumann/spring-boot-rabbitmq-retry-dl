package springbootrabbitmqretrydl;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Receiver {

    private boolean last = true;

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + "> at "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
        throw new RuntimeException("failed");
    }


}
