import static org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the methods of the Label class.
 */
public class LabelTest {
    private Label computer;
    /**
     * Set up object.
     */
    public void setUp() {
        computer = new Label(" Computer ");
    }
    /**
     * Tests constructor.
     */
    public void testConstructor() {
        assertEquals("computer", computer.getName());
    }
    /**
     * Tests constructor when name is null.
     */
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
    /**
     * Tests constructor when name is empty.
     */
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
    /**
     * Tests toString.
     */
    public void testToString() {
        assertEquals("@computer", computer.toString());
    }
    /**
     * Tests equals when objects are the same.
     */
    public void testEqualsSame() {
        assertTrue(computer.equals(computer));
    }
    /**
     * Tests equals when object is null.
     */
    public void testEqualsNull() {
        assertFalse(computer.equals(null));
    }
    /**
     * Tests equals when the classes are different.
     */
    public void testEqualsDiffClass() {
        assertFalse(computer.equals("hi"));
    }
    /**
     * Tests equals when the objects are not equal.
     */
    public void testEqualsFalse() {
        Label homework = new Label("homework");
        assertFalse(computer.equals(homework));
    }
    /**
     * Tests equals when objects are equal.
     */
    public void testEqualsTrue() {
        Label c2 = new Label("computer");
        assertTrue(computer.equals(c2));
    }
}