import static org.junit.Assert.*;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CompletedEntry: one normal case and at least one bad-input
 * case for each public method.
 *
 * @author Prateek
 * @version October 2026
 */
public class CompletedEntryTest
{
    private Task task;
    private LocalDate day;
    private CompletedEntry entry;

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
     * Creates an entry before each test.
     */
    @Before
    public void setUp()
    {
        task = new Task("Email advisor", 15, "low", tag("home"));
        day = LocalDate.of(2026, 10, 14);
        entry = new CompletedEntry(task, day);
    }

    @Test
    public void testConstructorAndGetters()
    {
        assertSame(task, entry.getTask());
        assertEquals(day, entry.getCompletedDate());
    }

    @Test
    public void testConstructorBadInput()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new CompletedEntry(null, day));
        assertThrows(IllegalArgumentException.class,
            () -> new CompletedEntry(task, null));
    }

    @Test
    public void testEquals()
    {
        Task same = new Task("Email advisor", 15, "low", tag("home"));
        CompletedEntry other = new CompletedEntry(same,
            LocalDate.of(2026, 10, 14));
        assertEquals(entry, other);
        assertEquals(entry.hashCode(), other.hashCode());
        assertEquals(entry, entry);
    }

    @Test
    public void testEqualsBadInput()
    {
        assertFalse(entry.equals(null));
        assertFalse(entry.equals(task));
        assertNotEquals(entry, new CompletedEntry(task, day.plusDays(1)));
        assertNotEquals(entry,
            new CompletedEntry(new Task("Other", 15, "low", tag("home")),
                day));
    }

    @Test
    public void testToString()
    {
        assertEquals("2026-10-14: Email advisor (15 min, low) @home",
            entry.toString());
    }
}
