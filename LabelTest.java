import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests the methods of the Label class.
 *
 * @author Ashna, Prateek
 * @version October 14, 2026
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

    @Test
    public void testConstructorNormalizes()
    {
        assertEquals("computer", computer.getName());
        assertEquals("laptop", new Label(" @Laptop ").getName());
        assertEquals("phone", new Label("@ phone").getName());
    }

    @Test
    public void testOnlyAtSign()
    {
        try
        {
            new Label("@");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            new Label(" @ ");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testTooLongName()
    {
        String longest = "a".repeat(Label.MAX_LENGTH);
        assertEquals(longest, new Label(longest).getName());
        try
        {
            new Label(longest + "a");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testNameWithSpace()
    {
        try
        {
            new Label("my laptop");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetName()
    {
        assertEquals("errand", new Label("ERRAND").getName());
    }

    @Test
    public void testEquals()
    {
        assertEquals(new Label(" @Laptop "), new Label("@laptop"));
        assertEquals(computer, computer);
    }

    @Test
    public void testEqualsBadInput()
    {
        assertNotEquals(computer, new Label("phone"));
        assertFalse(computer.equals(null));
        assertFalse(computer.equals("computer"));
    }

    @Test
    public void testToStringExtraAt()
    {
        // Extra '@' signs typed by the user are not doubled up.
        assertEquals("@laptop", new Label("@@laptop").toString());
    }
}
