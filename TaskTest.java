import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests methods of the Task class: one normal case and at least one
 * bad-input case for each public method.
 *
 * @author Ashna, Prateek
 * @version October 2026
 */
public class TaskTest {
    private Task task1;
    private LinkedChain<Label> labels1;
    private LinkedChain<Label> labels;
    private Task email;

    /**
     * Makes a chain with one label.
     *
     * @param name the label name
     * @return a chain holding that label
     */
    private static LinkedChain<Label> tag(String name)
    {
        LinkedChain<Label> chain = new LinkedChain<Label>();
        chain.add(new Label(name));
        return chain;
    }

    /**
     * Sets up objects.
     */
    @Before
    public void setUp() {
        labels1 = new LinkedChain<>();
        Label hw = new Label("homework");
        Label computer = new Label("computer");
        labels1.add(hw);
        labels1.add(computer);
        task1 = new Task("Finish math homework", 80, "low", labels1);

        labels = new LinkedChain<Label>();
        labels.addToEnd(new Label("@laptop"));
        labels.addToEnd(new Label("@phone"));
        email = new Task("  Email advisor ", 15, " LOW ", labels);
    }

    /**
     * Test getters.
     */
    @Test
    public void testGetters() {
        assertEquals("Finish math homework", task1.getDescription());
        assertEquals(80, task1.getMinutes());
        assertEquals("low", task1.getEnergy());
        // Task keeps its own copy of the labels, so compare the contents
        // rather than the chain object.
        assertEquals(labels1.size(), task1.getLabels().size());
        assertTrue(task1.getLabels().contains(new Label("homework")));
        assertTrue(task1.getLabels().contains(new Label("computer")));
    }
    /**
     * Test null description.
     */
    @Test
    public void testNullDescript() {
        IllegalArgumentException exception = null;
        try {
            Task newTask = new Task(null, 30, "low", labels1);
        }
        catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }
    /**
     * Test empty labels.
     */
    @Test
    public void testNoLabels() {
        LinkedChain<Label> empty = new LinkedChain<>();
        IllegalArgumentException exception = null;
        try {
            Task newTask = new Task("Sleep", 30, "low", empty);
        }
        catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    public void testConstructor()
    {
        assertEquals("Email advisor", email.getDescription());
        assertEquals(15, email.getMinutes());
        assertEquals("low", email.getEnergy());
        assertEquals(2, email.getLabels().size());
    }

    @Test
    public void testConstructorCopiesLabels()
    {
        labels.add(new Label("@errand"));
        assertEquals(2, email.getLabels().size());
        assertNotSame(labels, email.getLabels());
    }

    @Test
    public void testConstructorDuplicateLabels()
    {
        LinkedChain<Label> dupes = new LinkedChain<Label>();
        dupes.add(new Label("@home"));
        dupes.add(new Label("HOME"));
        assertEquals(1, new Task("Dishes", 10, "low", dupes)
            .getLabels().size());
    }

    @Test
    public void testConstructorNoLabels()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 30, "high", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 30, "high", new LinkedChain<Label>()));
    }

    @Test
    public void testConstructorBadDescription()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Task(null, 15, "low", labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("   ", 15, "low", labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("x".repeat(Task.MAX_DESCRIPTION + 1), 15, "low",
                labels));
    }

    @Test
    public void testConstructorBadMinutes()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 0, "low", labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", -15, "low", labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 481, "low", labels));
        assertEquals(1, new Task("Read", 1, "low", tag("home")).getMinutes());
        assertEquals(480,
            new Task("Read", 480, "low", tag("home")).getMinutes());
    }

    @Test
    public void testConstructorBadEnergy()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 15, null, labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 15, "medium", labels));
        assertThrows(IllegalArgumentException.class,
            () -> new Task("Read", 15, "", labels));
        assertEquals("high",
            new Task("Read", 15, "High", tag("home")).getEnergy());
    }

    @Test
    public void testIsValidEnergy()
    {
        assertTrue(Task.isValidEnergy("low"));
        assertTrue(Task.isValidEnergy(" HIGH "));
        assertFalse(Task.isValidEnergy("lowish"));
        assertFalse(Task.isValidEnergy(null));
    }

    @Test
    public void testHasLabel()
    {
        assertTrue(email.hasLabel(new Label("Laptop")));
        assertFalse(email.hasLabel(new Label("errand")));
        assertFalse(email.hasLabel(null));
    }

    @Test
    public void testAddLabel()
    {
        assertTrue(email.addLabel(new Label("@errand")));
        assertEquals(3, email.getLabels().size());
        assertTrue(email.hasLabel(new Label("errand")));
    }

    @Test
    public void testAddLabelBadInput()
    {
        assertFalse(email.addLabel(null));
        assertFalse(email.addLabel(new Label("@LAPTOP")));
        assertEquals(2, email.getLabels().size());
    }

    @Test
    public void testRemoveLabel()
    {
        assertTrue(email.removeLabel(new Label("phone")));
        assertEquals(1, email.getLabels().size());
        assertFalse(email.hasLabel(new Label("phone")));
    }

    @Test
    public void testRemoveLabelBadInput()
    {
        assertFalse(email.removeLabel(null));
        assertFalse(email.removeLabel(new Label("errand")));
        assertEquals(2, email.getLabels().size());
        // The last label can never be removed.
        Task one = new Task("Read", 30, "high", tag("home"));
        assertFalse(one.removeLabel(new Label("home")));
        assertEquals(1, one.getLabels().size());
    }

    @Test
    public void testEquals()
    {
        LinkedChain<Label> reversed = new LinkedChain<Label>();
        reversed.add(new Label("laptop"));
        reversed.add(new Label("phone"));
        Task same = new Task("email ADVISOR", 15, "low", reversed);
        assertEquals(email, same);
        assertEquals(email.hashCode(), same.hashCode());
        assertEquals(email, email);
    }

    @Test
    public void testEqualsBadInput()
    {
        assertFalse(email.equals(null));
        assertFalse(email.equals("Email advisor"));
        assertNotEquals(email, new Task("Email advisor", 20, "low", labels));
        assertNotEquals(email, new Task("Email advisor", 15, "high", labels));
        assertNotEquals(email, new Task("Email boss", 15, "low", labels));
        assertNotEquals(email,
            new Task("Email advisor", 15, "low", tag("home")));
    }

    @Test
    public void testToString()
    {
        assertEquals("Email advisor (15 min, low) @laptop @phone",
            email.toString());
        assertEquals("Read (30 min, high) @home",
            new Task("Read", 30, "high", tag("home")).toString());
    }

    @Test
    public void testGettersHighEnergy()
    {
        Task t = new Task("Call mom", 480, "high", tag("home"));
        assertEquals("Call mom", t.getDescription());
        assertEquals(480, t.getMinutes());
        assertEquals("high", t.getEnergy());
        assertNotNull(t.getLabels());
    }
}
