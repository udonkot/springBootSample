import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class SampleMain {

    @RequestMapping("/")
    String home() {
        StringBuilder sb = new StringBuilder("Hello Spring-Boot!!").append("\r\n").append("Update!");
        return sb.toString();
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleMain.class, args);
    }
}
