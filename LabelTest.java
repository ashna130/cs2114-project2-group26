import static org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LabelTest {
    public void setUp() {
        Label computer = new Label(" Computer ");
    }
    public void testConstructorNull() {
        boolean thrown = false;
        try {
            Label word = null;
        }
        catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}