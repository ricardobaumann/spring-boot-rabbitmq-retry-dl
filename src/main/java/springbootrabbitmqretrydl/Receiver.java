package springbootrabbitmqretrydl;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Receiver {

    public void receiveMessage(String message) {
        if (Integer.parseInt(message)%2==0) {
            System.out.println("Received even <" + message + "> at "+new SimpleDateFormat("HH:mm:ss").format(new Date()));

        } else {
            System.err.println("Received odd number "+message);
            //throw new RuntimeException("failed");
        }
    }


}
