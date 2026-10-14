import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests methods of the Task class.
 */
public class TaskTest {
    private Task task1;
    private LinkedChain<Label> labels1;
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
    }
    /**
     * Test getters.
     */
    @Test
    public void testGetters() {
        assertEquals("Finish math homework", task1.getDescription());
        assertEquals(80, task1.getMinutes());
        assertEquals("low", task1.getEnergy());
        assertEquals(labels1, task1.getLabels());
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
}