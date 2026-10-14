import static org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LabelTest {
    public void setUp() {
        Label computer = new Label(" Computer ");
    }
    public void testConstructor() {
        assertEquals("computer", computer.getName());
    }
    public void testNullName() {
        IllegalArgumentException exception = null
        try {
            Label word = new Label(null);
        }
        catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
    public void testEmptyName() {
        IllegalArgumentException exception = null
        try {
            Label word = new Label("  ");
        }
        catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
    public void testToString() {
        assertEquals("@computer", computer.toString());
    }
}