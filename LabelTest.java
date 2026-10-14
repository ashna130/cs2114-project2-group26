import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests the methods of the Label class.
 */
public class LabelTest {
    private Label computer;
    /**
     * Set up object.
     */
    @Before
    public void setUp() {
        computer = new Label(" Computer ");
    }
    /**
     * Tests constructor.
     */
    @Test
    public void testConstructor() {
        assertEquals("computer", computer.getName());
    }
    /**
     * Tests constructor when name is null.
     */
    @Test
    public void testNullName() {
        IllegalArgumentException exception = null;
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
    @Test
    public void testEmptyName() {
        IllegalArgumentException exception = null;
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
    @Test
    public void testToString() {
        assertEquals("@computer", computer.toString());
    }
    /**
     * Tests equals when objects are the same.
     */
    @Test
    public void testEqualsSame() {
        assertTrue(computer.equals(computer));
    }
    /**
     * Tests equals when object is null.
     */
    @Test
    public void testEqualsNull() {
        assertFalse(computer.equals(null));
    }
    /**
     * Tests equals when the classes are different.
     */
    @Test
    public void testEqualsDiffClass() {
        assertFalse(computer.equals("hi"));
    }
    /**
     * Tests equals when the objects are not equal.
     */
    @Test
    public void testEqualsFalse() {
        Label homework = new Label("homework");
        assertFalse(computer.equals(homework));
    }
    /**
     * Tests equals when objects are equal.
     */
    @Test
    public void testEqualsTrue() {
        Label c2 = new Label("computer");
        assertTrue(computer.equals(c2));
    }
}