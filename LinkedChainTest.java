import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the provided LinkedChain, following section 1 of our test
 * plan. These run with Strings and Integers, before the app exists, to
 * show the chain is generic.
 *
 * @author Prateek
 * @version October 2026
 */
public class LinkedChainTest
{
    private LinkedChain<String> chain;

    /**
     * Creates an empty chain before each test.
     */
    @Before
    public void setUp()
    {
        chain = new LinkedChain<String>();
    }

    /**
     * Builds a chain holding a, b, c in that order.
     */
    private void addABC()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
    }

    @Test
    public void testNewChainIsEmpty()
    {
        assertTrue(chain.isEmpty());
        assertEquals(0, chain.size());
    }

    @Test
    public void testRemoveFromEmpty()
    {
        assertFalse(chain.remove("x"));
        assertNull(chain.remove());
        assertEquals(0, chain.size());
    }

    @Test
    public void testAddOne()
    {
        assertTrue(chain.add("a"));
        assertEquals(1, chain.size());
        assertTrue(chain.contains("a"));
        assertFalse(chain.isEmpty());
    }

    @Test
    public void testRemoveOnly()
    {
        chain.add("a");
        assertTrue(chain.remove("a"));
        assertTrue(chain.isEmpty());
    }

    @Test
    public void testRemoveHead()
    {
        addABC();
        assertTrue(chain.remove("a"));
        assertEquals("[b, c]", chain.toString());
    }

    @Test
    public void testRemoveMiddle()
    {
        addABC();
        assertTrue(chain.remove("b"));
        assertEquals("[a, c]", chain.toString());
        assertEquals("c", chain.get(1));
    }

    @Test
    public void testRemoveTail()
    {
        addABC();
        assertTrue(chain.remove("c"));
        assertEquals("[a, b]", chain.toString());
    }

    @Test
    public void testRemoveAbsent()
    {
        addABC();
        assertFalse(chain.remove("z"));
        assertFalse(chain.remove(null));
        assertEquals(3, chain.size());
    }

    @Test
    public void testDuplicatesCount()
    {
        chain.add("a");
        chain.add("a");
        chain.add("b");
        assertEquals(2, chain.count("a"));
        assertEquals(1, chain.count("b"));
        assertEquals(0, chain.count("z"));
        assertEquals(0, chain.count(null));
    }

    @Test
    public void testRemoveOneDuplicate()
    {
        chain.add("a");
        chain.add("a");
        assertTrue(chain.remove("a"));
        assertEquals(1, chain.count("a"));
    }

    /**
     * The provided chain throws on null instead of returning false (our
     * spec said false). The app never passes null, so we kept it.
     */
    @Test
    public void testAddNull()
    {
        assertThrows(IllegalArgumentException.class, () -> chain.add(null));
        assertThrows(IllegalArgumentException.class,
            () -> chain.addToEnd(null));
        assertEquals(0, chain.size());
    }

    @Test
    public void testAddThousand()
    {
        LinkedChain<Integer> numbers = new LinkedChain<Integer>();
        for (int i = 0; i < 1000; i++)
        {
            numbers.add(i);
        }
        assertEquals(1000, numbers.size());
        assertTrue(numbers.contains(0));
        assertTrue(numbers.contains(999));
    }

    @Test
    public void testToArray()
    {
        assertEquals(0, chain.toArray(new String[0]).length);
        chain.add("a");
        assertArrayEquals(new String[] {"a"}, chain.toArray(new String[1]));
        addABC();
        assertArrayEquals(new String[] {"a", "a", "b", "c"},
            chain.toArray(new String[4]));
    }

    @Test
    public void testToArrayBadInput()
    {
        addABC();
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(null));
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(new String[2]));
    }

    @Test
    public void testGetOutOfBounds()
    {
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(0));
        chain.add("a");
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(1));
    }

    @Test
    public void testClear()
    {
        addABC();
        chain.clear();
        assertTrue(chain.isEmpty());
        assertEquals("[]", chain.toString());
    }
}
