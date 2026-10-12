import static org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LabelTest {
    public void setUp() {
        Label computer = new Label(" Computer ");
    }
    public void testConstructorNull() {
        IllegalArgumentException exception = null
        try {
            Label word = null;
        }
        catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
}