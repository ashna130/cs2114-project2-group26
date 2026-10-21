import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LooseEnds: one normal case and at least one bad-input case
 * for each public method, plus sections 3 and 4 of our test plan (logic
 * tests and scripted "manual" input through the menu).
 *
 * @author Prateek, Ashna
 * @version October 14, 2026
 */
public class LooseEndsTest
{
    private LooseEnds app;
    private LinkedChain<Label> laptop;
    private LinkedChain<Label> laptopPhone;
    private LinkedChain<Label> chores;

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
     * Creates an app with a seeded random before each test.
     */
    @Before
    public void setUp()
    {
        app = new LooseEnds(new Random(42));
        laptop = tag("@laptop");
        laptopPhone = new LinkedChain<Label>();
        laptopPhone.add(new Label("@laptop"));
        laptopPhone.add(new Label("@phone"));
        chores = new LinkedChain<Label>();
        chores.add(new Label("@errand"));
    }

    // ---------------------------------------------------------- constructors

    @Test
    public void testConstructors()
    {
        assertEquals(0, new LooseEnds().countOpen());
        LooseEnds nullRandom = new LooseEnds(null);
        assertEquals(0, nullRandom.countOpen());
        nullRandom.addTask("Read", 10, "low", tag("home"));
        assertNotNull(nullRandom.whatNow(10, null, "low"));
    }

    // --------------------------------------------------------------- addTask

    @Test
    public void testAddTask()
    {
        Task t = app.addTask("Email advisor", 15, "low", laptopPhone);
        assertNotNull(t);
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countLabel(new Label("laptop")));
        assertEquals(1, app.countLabel(new Label("phone")));
    }

    @Test
    public void testAddTaskDuplicatesAllowed()
    {
        app.addTask("Email advisor", 15, "low", laptop);
        app.addTask("Email advisor", 15, "low", laptop);
        assertEquals(2, app.countOpen());
        assertEquals(2, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testAddTaskBadInput()
    {
        assertNull(app.addTask("   ", 15, "low", laptop));
        assertNull(app.addTask(null, 15, "low", laptop));
        assertNull(app.addTask("Read", 0, "low", laptop));
        assertNull(app.addTask("Read", -15, "low", laptop));
        assertNull(app.addTask("Read", 100000, "low", laptop));
        assertNull(app.addTask("Read", 15, "medium", laptop));
        assertNull(app.addTask("Read", 15, "low", null));
        assertNull(app.addTask("Read", 15, "low", new LinkedChain<Label>()));
        assertEquals(0, app.countOpen());
        assertEquals(0, app.countLabel(new Label("laptop")));
    }

    // ---------------------------------------------------------------- isOpen

    @Test
    public void testIsOpen()
    {
        Task t = app.addTask("Read", 10, "low", laptop);
        assertTrue(app.isOpen(t));
        assertTrue(app.isOpen(new Task("read", 10, "low", laptop)));
    }

    @Test
    public void testIsOpenBadInput()
    {
        assertFalse(app.isOpen(null));
        assertFalse(app.isOpen(new Task("Read", 10, "low", laptop)));
        Task t = app.addTask("Read", 10, "low", laptop);
        app.deleteTask(t);
        assertFalse(app.isOpen(t));
    }

    // -------------------------------------------------- addLabel/removeLabel

    @Test
    public void testAddLabel()
    {
        Task t = app.addTask("Read", 30, "low", tag("home"));
        assertTrue(app.addLabel(t, "@Couch"));
        assertTrue(t.hasLabel(new Label("couch")));
        assertEquals(1, app.countLabel(new Label("couch")));
    }

    @Test
    public void testAddLabelBadInput()
    {
        Task t = app.addTask("Read", 30, "low", laptop);
        assertFalse(app.addLabel(t, "@laptop"));       // already there
        assertFalse(app.addLabel(t, " @LAPTOP "));     // same, other case
        assertFalse(app.addLabel(t, "   "));           // blank
        assertFalse(app.addLabel(t, null));
        assertFalse(app.addLabel(t, "x".repeat(200))); // too long
        assertFalse(app.addLabel(null, "@phone"));
        Task notOpen = new Task("Other", 5, "low", laptop);
        assertFalse(app.addLabel(notOpen, "@phone"));
        assertEquals(1, app.countLabel(new Label("laptop")));
        assertEquals(0, app.countLabel(new Label("phone")));
    }

    @Test
    public void testRemoveLabel()
    {
        Task t = app.addTask("Email advisor", 15, "low", laptopPhone);
        assertTrue(app.removeLabel(t, "PHONE"));
        assertFalse(t.hasLabel(new Label("phone")));
        assertEquals(0, app.countLabel(new Label("phone")));
        assertEquals(1, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testRemoveLabelBadInput()
    {
        Task t = app.addTask("Email advisor", 15, "low", laptop);
        assertFalse(app.removeLabel(t, "@errand")); // not on task
        assertFalse(app.removeLabel(t, "@laptop")); // its only label
        assertFalse(app.removeLabel(t, ""));
        assertFalse(app.removeLabel(t, null));
        assertFalse(app.removeLabel(null, "@laptop"));
        assertEquals(1, app.countLabel(new Label("laptop")));
        app.deleteTask(t);
        assertFalse(app.removeLabel(t, "@laptop")); // task no longer open
    }

    // ------------------------------------------------------------ deleteTask

    @Test
    public void testDeleteTaskDropsLabelCount()
    {
        Task t = app.addTask("Email advisor", 15, "low", laptopPhone);
        app.addTask("Charge laptop", 5, "low", laptop);
        assertEquals(2, app.countLabel(new Label("laptop")));
        assertTrue(app.deleteTask(t));
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countLabel(new Label("laptop")));
        assertEquals(0, app.countLabel(new Label("phone")));
        assertEquals(0, app.countDone());
    }

    @Test
    public void testDeleteTaskBadInput()
    {
        assertFalse(app.deleteTask(null));
        assertFalse(app.deleteTask(new Task("Ghost", 5, "low", laptop)));
        Task t = app.addTask("Read", 30, "low", laptop);
        assertTrue(app.deleteTask(t));
        assertFalse(app.deleteTask(t));
        assertEquals(0, app.countOpen());
    }

    // ---------------------------------------------------------- completeTask

    @Test
    public void testCompleteTaskMovesToDone()
    {
        Task t = app.addTask("Email advisor", 15, "low", laptop);
        app.addTask("Read", 30, "low", tag("home"));
        assertTrue(app.completeTask(t));
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countDone());
        assertEquals(0, app.countLabel(new Label("laptop")));
        CompletedEntry entry = app.getDonePile()[0];
        assertSame(t, entry.getTask());
        assertEquals(LocalDate.now(), entry.getCompletedDate());
    }

    @Test
    public void testCompleteTwiceReportsGone()
    {
        // Two identical tasks: completing one twice must not take the
        // second, unrelated copy.
        Task t = app.addTask("Email advisor", 15, "low", laptop);
        Task other = app.addTask("Read", 30, "low", laptop);
        assertTrue(app.completeTask(t));
        assertFalse(app.completeTask(t));
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countDone());
        assertSame(other, app.getOpenTasks()[0]);
        assertEquals(1, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testCompleteTaskBadInput()
    {
        assertFalse(app.completeTask(null));
        assertFalse(app.completeTask(new Task("Ghost", 5, "low", laptop)));
        Task t = app.addTask("Read", 30, "low", laptop);
        assertFalse(app.completeTask(t, null));
        assertEquals(1, app.countOpen());
        assertEquals(0, app.countDone());
    }

    @Test
    public void testCompleteTaskWithDate()
    {
        Task t = app.addTask("Read", 30, "low", laptop);
        LocalDate day = LocalDate.of(2026, 10, 1);
        assertTrue(app.completeTask(t, day));
        assertEquals(day, app.getDonePile()[0].getCompletedDate());
    }

    @Test
    public void testCompleteRecurringTask()
    {
        Task laundry = app.addTask("Fold laundry", 15, "low", chores, true);
        assertNotNull(laundry);
        assertTrue(app.completeTask(laundry));
        assertEquals(1, app.countDone());
        assertEquals(1, app.countOpen());
        Task recurred = app.getOpenTasks()[0];
        assertTrue(recurred.isRecurring());
        assertEquals(laundry.getDescription(), recurred.getDescription());
        assertEquals(laundry.getMinutes(), recurred.getMinutes());
        assertEquals(laundry.getEnergy(), recurred.getEnergy());
        assertTrue(recurred.hasLabel(new Label("@errand");
    }

    @Test
    public void testCompleteTaskTwice()
    {
        Task laundry = app.addTask("Fold laundry", 15, "low", chores, true);
        assertTrue(app.completeTask(laundry));
        assertFalse(app.completeTask(laundry));
        assertEquals(0, app.countOpen());
        assertEquals(1, app.countDone());
        assertEquals(0, app.countLabel(new Label("@errand")));
    }

    // -------------------------------------------------- undo (stretch goal)

    @Test
    public void testUndoComplete()
    {
        Task t = app.addTask("Read", 30, "low", laptop);
        app.completeTask(t);
        assertEquals("completed: " + t, app.undoDescription());
        assertTrue(app.undo());
        assertTrue(app.isOpen(t));
        assertEquals(0, app.countDone());
        assertEquals(1, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testUndoDelete()
    {
        Task t = app.addTask("Read", 30, "low", laptop);
        app.deleteTask(t);
        assertEquals("deleted: " + t, app.undoDescription());
        assertTrue(app.undo());
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testUndoBadInput()
    {
        assertNull(app.undoDescription());
        assertFalse(app.undo());            // nothing to undo
        Task t = app.addTask("Read", 30, "low", laptop);
        app.deleteTask(t);
        assertTrue(app.undo());
        assertFalse(app.undo());            // only one level of undo
        assertEquals(1, app.countOpen());
        app.completeTask(t);
        app.clearDone();
        assertFalse(app.undo());            // entry was cleared
        assertEquals(0, app.countOpen());
        assertNull(app.undoDescription());
    }

    @Test
    public void testUndoRecurring()
    {
        Task laundry = app.addTask("Fold laundry", 15, "low", chores, true);
        assertTrue(app.completeTask(task));
        assertEquals(0, app.countOpen());
        assertEquals(1, app.countDone());
        assertTrue(app.undo());
        assertEquals(1, app.countOpen());
        assertEquals(0, app.countDone());
        assertEquals(laundry, app.getOpenTasks()[0]);
    }

    // -------------------------------------------------------------- whatNow

    @Test
    public void testWhatNowMatchesAllFilters()
    {
        Task fits = app.addTask("Email advisor", 15, "low", laptop);
        app.addTask("Long essay", 120, "low", laptop);      // too long
        app.addTask("Gym", 15, "high", laptop);             // too tiring
        app.addTask("Call mom", 15, "low", tag("phone"));   // wrong label
        for (int i = 0; i < 20; i++)
        {
            assertSame(fits, app.whatNow(30, new Label("laptop"), "low"));
        }
    }

    @Test
    public void testWhatNowHighEnergyAndAnyLabel()
    {
        Task gym = app.addTask("Gym", 15, "high", tag("out"));
        Task email = app.addTask("Email advisor", 15, "low", laptop);
        // Low energy never gets the high-energy task.
        for (int i = 0; i < 20; i++)
        {
            assertSame(email, app.whatNow(15, null, "low"));
        }
        // High energy can get either one, and over many spins gets both.
        boolean sawGym = false;
        boolean sawEmail = false;
        for (int i = 0; i < 50; i++)
        {
            Task pick = app.whatNow(15, null, " HIGH ");
            sawGym = sawGym || pick == gym;
            sawEmail = sawEmail || pick == email;
        }
        assertTrue(sawGym);
        assertTrue(sawEmail);
    }

    @Test
    public void testWhatNowNoMatch()
    {
        app.addTask("Long essay", 120, "low", laptop);
        assertNull(app.whatNow(30, new Label("laptop"), "low"));
        assertNull(app.whatNow(480, new Label("phone"), "high"));
    }

    @Test
    public void testWhatNowEmptyBag()
    {
        assertNull(app.whatNow(30, null, "low"));
        assertNull(app.another());
    }

    @Test
    public void testWhatNowBadInput()
    {
        app.addTask("Read", 10, "low", laptop);
        assertNull(app.whatNow(0, null, "low"));
        assertNull(app.whatNow(-5, null, "low"));
        assertNull(app.whatNow(481, null, "low"));
        assertNull(app.whatNow(30, null, null));
        assertNull(app.whatNow(30, null, "sleepy"));
    }

    // -------------------------------------------------------------- another

    @Test
    public void testAnotherNeverRepeats()
    {
        app.addTask("A", 10, "low", laptop);
        app.addTask("B", 10, "low", laptop);
        app.addTask("C", 10, "low", laptop);
        Task previous = app.whatNow(10, null, "low");
        assertNotNull(previous);
        for (int i = 0; i < 30; i++)
        {
            Task next = app.another();
            assertNotNull(next);
            assertNotEquals(previous, next);
            previous = next;
        }
    }

    @Test
    public void testAnotherBadInput()
    {
        assertNull(app.another()); // no whatNow yet
        app.addTask("Only one", 10, "low", laptop);
        assertNotNull(app.whatNow(10, null, "low"));
        assertNull(app.another()); // nothing else matches
    }

    // ------------------------------------------------ mostRestrictiveFilter

    @Test
    public void testMostRestrictiveFilter()
    {
        app.addTask("Essay", 120, "low", laptop);
        app.addTask("Report", 90, "low", laptop);
        app.addTask("Call mom", 5, "low", tag("phone"));
        // 5 minutes rules out 2 tasks; @laptop only rules out 1.
        assertEquals("time",
            app.mostRestrictiveFilter(5, new Label("laptop"), "low"));
        // @errand rules out all 3.
        assertEquals("label",
            app.mostRestrictiveFilter(480, new Label("errand"), "low"));
        LooseEnds tired = new LooseEnds(new Random(1));
        tired.addTask("Gym", 30, "high", tag("out"));
        tired.addTask("Run", 30, "high", tag("out"));
        assertEquals("energy",
            tired.mostRestrictiveFilter(60, null, "low"));
    }

    @Test
    public void testMostRestrictiveFilterBadInput()
    {
        assertNull(app.mostRestrictiveFilter(30, null, "low")); // empty bag
        app.addTask("Read", 10, "low", laptop);
        assertNull(app.mostRestrictiveFilter(0, null, "low"));
        assertNull(app.mostRestrictiveFilter(30, null, "medium"));
        assertNull(app.mostRestrictiveFilter(30, null, null));
    }

    // --------------------------------------------------- counts and getters

    @Test
    public void testCountOpen()
    {
        assertEquals(0, app.countOpen());
        app.addTask("Read", 10, "low", laptop);
        app.addTask("", 10, "low", laptop); // rejected
        assertEquals(1, app.countOpen());
    }

    @Test
    public void testCountLabel()
    {
        app.addTask("A", 10, "low", laptop);
        app.addTask("B", 10, "low", laptopPhone);
        // " @Laptop " and "@laptop" count as one label, not two.
        assertEquals(2, app.countLabel(new Label(" @Laptop ")));
        assertEquals(1, app.countLabel(new Label("phone")));
    }

    @Test
    public void testCountLabelBadInput()
    {
        assertEquals(0, app.countLabel(null));
        assertEquals(0, app.countLabel(new Label("errand"))); // empty bag
    }

    @Test
    public void testCountDone()
    {
        assertEquals(0, app.countDone());
        Task t = app.addTask("Read", 10, "low", laptop);
        app.completeTask(t);
        app.completeTask(t);
        assertEquals(1, app.countDone());
    }

    @Test
    public void testGetOpenTasksAndDonePile()
    {
        assertEquals(0, app.getOpenTasks().length);
        assertEquals(0, app.getDonePile().length);
        Task t = app.addTask("Read", 10, "low", laptop);
        app.addTask("Write", 10, "low", laptop);
        assertEquals(2, app.getOpenTasks().length);
        app.completeTask(t);
        assertEquals(1, app.getOpenTasks().length);
        assertEquals(1, app.getDonePile().length);
    }

    // ----------------------------------------- doneSummary / weeklySummary

    @Test
    public void testDoneSummary()
    {
        Task a = app.addTask("Email advisor", 15, "low", laptopPhone);
        Task b = app.addTask("Charge laptop", 5, "low", laptop);
        app.completeTask(a, LocalDate.of(2026, 1, 1));
        app.completeTask(b);
        String summary = app.doneSummary();
        assertTrue(summary.contains("2 task(s) closed in total"));
        assertTrue(summary.contains("@laptop: 2"));
        assertTrue(summary.contains("@phone: 1"));
    }

    @Test
    public void testDoneSummaryEmpty()
    {
        assertEquals("Nothing in the pile yet.", app.doneSummary());
    }

    @Test
    public void testWeeklySummary()
    {
        LocalDate today = LocalDate.of(2026, 10, 14);
        Task a = app.addTask("Email advisor", 15, "low", laptopPhone);
        Task b = app.addTask("Charge laptop", 5, "low", laptop);
        Task old = app.addTask("Old thing", 60, "low", tag("errand"));
        app.completeTask(a, today);
        app.completeTask(b, today.minusDays(6));
        app.completeTask(old, today.minusDays(7)); // outside the week
        String summary = app.weeklySummary(today);
        assertTrue(summary.contains("Tasks finished: 2"));
        assertTrue(summary.contains("Minutes spent: 20"));
        assertTrue(summary.contains("@laptop: 2"));
        assertTrue(summary.contains("@phone: 1"));
        assertFalse(summary.contains("@errand"));
    }

    @Test
    public void testWeeklySummaryEmptyAndBadInput()
    {
        assertEquals("Nothing in the pile yet.", app.weeklySummary());
        assertEquals("No date given.", app.weeklySummary(null));
        Task t = app.addTask("Old", 10, "low", laptop);
        app.completeTask(t, LocalDate.of(2020, 1, 1));
        String summary = app.weeklySummary(LocalDate.of(2026, 10, 14));
        assertTrue(summary.contains("Tasks finished: 0"));
        assertTrue(summary.contains("No labels in this period."));
    }

    // --------------------------------------- avoidance report (stretch goal)

    @Test
    public void testMostAvoidedLabel()
    {
        app.addTask("Groceries", 30, "high", tag("errand"));
        app.addTask("Post office", 20, "low", tag("errand"));
        Task e = app.addTask("Email", 10, "low", laptop);
        app.addTask("Slides", 60, "high", laptop);
        app.completeTask(e);
        // @errand: 2 open, 0 done.  @laptop: 1 open, 1 done.
        assertEquals(new Label("errand"), app.mostAvoidedLabel());
        assertEquals("You keep skipping @errand: 2 open, 0 done.",
            app.avoidanceReport());
    }

    @Test
    public void testMostAvoidedLabelEmpty()
    {
        assertNull(app.mostAvoidedLabel());
        assertEquals("No open tasks, so nothing is being avoided.",
            app.avoidanceReport());
        Task t = app.addTask("Read", 10, "low", laptop);
        app.completeTask(t);
        assertNull(app.mostAvoidedLabel()); // done pile only
    }

    // ------------------------------------------------------------ clearDone

    @Test
    public void testClearDoneLeavesOpenTasks()
    {
        Task t = app.addTask("Read", 10, "low", laptop);
        app.addTask("Write", 10, "low", laptop);
        app.completeTask(t);
        app.clearDone();
        assertEquals(0, app.countDone());
        assertEquals(1, app.countOpen());
        assertEquals(1, app.countLabel(new Label("laptop")));
    }

    @Test
    public void testClearDoneWhenEmpty()
    {
        app.clearDone();
        assertEquals(0, app.countDone());
        assertEquals("Nothing in the pile yet.", app.doneSummary());
    }

    // ------------------------------------------------- run (scripted input)

    /**
     * Runs the menu with the given typed input and returns everything it
     * printed.
     *
     * @param typed the lines the user types, separated by newlines
     * @return the program output
     */
    private String runWith(String typed)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        app.run(new Scanner(typed), out);
        out.flush();
        return bytes.toString();
    }

    /**
     * Counts how many times some text appears in the output.
     *
     * @param output the output
     * @param text   the text to count
     * @return the number of times it appears
     */
    private int occurrences(String output, String text)
    {
        int count = 0;
        int index = output.indexOf(text);
        while (index != -1)
        {
            count++;
            index = output.indexOf(text, index + text.length());
        }
        return count;
    }

    @Test
    public void testRunAddAndComplete()
    {
        String output = runWith(
            "1\nEmail advisor\n15\nlow\n@laptop @phone\n"
            + "5\n1\n0\n");
        assertTrue(output.contains("Added: Email advisor"));
        assertTrue(output.contains("Completed: Email advisor"));
        assertTrue(output.contains("Goodbye!"));
        assertEquals(0, app.countOpen());
        assertEquals(1, app.countDone());
    }

    @Test
    public void testRunBadMinutesRePrompts()
    {
        String output = runWith(
            "1\nRead\nthirty\nabt 20\n0\n-15\n100000\n\n30\nlow\n@home\n0\n");
        assertEquals(6,
            occurrences(output, "whole number from 1 to 480"));
        assertTrue(output.contains("\"thirty\" is not a number."));
        assertTrue(output.contains("\"abt 20\" is not a number."));
        assertTrue(output.contains("0 is out of range."));
        assertTrue(output.contains("-15 is out of range."));
        assertTrue(output.contains("100000 is out of range."));
        assertTrue(output.contains("Nothing was entered."));
        // The description typed before the bad minutes was kept.
        assertEquals("Read", app.getOpenTasks()[0].getDescription());
        assertEquals(30, app.getOpenTasks()[0].getMinutes());
    }

    @Test
    public void testRunBlankDescriptionEnergyAndLabels()
    {
        String output = runWith(
            "1\n   \nRead\n10\nmedium\nHIGH\n\n"
            + "@ " + "x".repeat(200) + "\n@Home\n0\n");
        assertTrue(output.contains("A task needs a description."));
        assertTrue(output.contains("Please type low or high."));
        assertTrue(output.contains("Skipping bad label"));
        assertEquals(2, occurrences(output, "at least one label."));
        assertEquals("high", app.getOpenTasks()[0].getEnergy());
        assertEquals(1, app.countLabel(new Label("home")));
    }

    @Test
    public void testRunBadMenuInput()
    {
        String output = runWith("abc\n99\n-1\n"
            + "1".repeat(5000) + "\n\n0\n");
        assertEquals(5, occurrences(output, "number from 0 to 13"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    public void testRunEndOfInput()
    {
        // Ctrl-D at the menu, and in the middle of adding a task.
        assertTrue(runWith("").contains("Goodbye!"));
        assertTrue(runWith("1\nRead\n").contains("Goodbye!"));
        assertEquals(0, app.countOpen());
    }

    @Test
    public void testRunCancelGoesBack()
    {
        String output = runWith("1\nRead\n10\ncancel\n2\n0\n");
        assertTrue(output.contains("Cancelled. Back to the menu."));
        assertEquals(0, app.countOpen());
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    public void testRunDuplicatePrompt()
    {
        String output = runWith(
            "1\nRead\n10\nlow\n@ok\n"
            + "1\nRead\n10\nlow\n@ok\nn\n"
            + "1\nread\n10\nlow\n@OK\ny\n0\n");
        assertEquals(2, occurrences(output, "Add anyway?"));
        assertTrue(output.contains("Not added."));
        assertEquals(2, app.countOpen());
        assertEquals(2, app.countLabel(new Label("ok")));
    }

    @Test
    public void testRunRouletteAndEmptyActions()
    {
        String output = runWith("3\n4\n5\n6\n10\n12\n13\n"
            + "1\nRead\n10\nlow\n@couch\n"
            + "3\n20\nlow\n@couch\n"
            + "4\n9\ncouch\n0\n");
        assertTrue(output.contains("Nothing in the pile yet. Add a task"));
        assertTrue(output.contains("Use What now? first."));
        assertTrue(output.contains("No open tasks."));
        assertTrue(output.contains("Nothing in the pile yet."));
        assertTrue(output.contains("nothing is being avoided"));
        assertTrue(output.contains("Nothing to undo."));
        assertTrue(output.contains("How about: Read"));
        assertTrue(output.contains("No other task matches."));
        assertTrue(output.contains("1 open task(s) have @couch."));
    }

    @Test
    public void testRunNoMatchOffersToLoosen()
    {
        app.addTask("Essay", 60, "low", laptop);
        // 5 minutes is too short: the time filter is named and loosened.
        String output = runWith("3\n5\nlow\n@laptop\ny\n"
            + "3\n5\nlow\n@laptop\nn\n0\n");
        assertEquals(2, occurrences(output,
            "Your time filter is the most restrictive."));
        assertEquals(1, occurrences(output, "How about: Essay"));
    }

    @Test
    public void testRunLabelMenusAndTaskChoice()
    {
        app.addTask("Read", 10, "low", tag("home"));
        String output = runWith("7\n9\nabc\n1\n@couch\n"
            + "7\n1\n@couch\n"
            + "8\n1\n@phone\n"
            + "8\n1\n@@\ncouch\n"
            + "8\n1\nhome\n"
            + "6\n\n6\n1\n0\n");
        assertTrue(output.contains("No task 9."));
        assertTrue(output.contains("No task abc."));
        assertTrue(output.contains("Label added."));
        assertTrue(output.contains("already has @couch"));
        assertTrue(output.contains("does not have @phone"));
        assertTrue(output.contains("not a valid label"));
        assertTrue(output.contains("Label removed."));
        assertTrue(output.contains("needs at least one label, so @home"));
        assertTrue(output.contains("Deleted: Read"));
        assertEquals(0, app.countOpen());
    }

    @Test
    public void testRunDonePileUndoAndAvoidance()
    {
        String output = runWith("1\nGroceries\n30\nhigh\n@errand\n"
            + "1\nEmail\n10\nlow\n@laptop\n"
            + "5\n1\n10\n12\n13\n13\n11\n0\n");
        assertTrue(output.contains("1 task(s) closed in total"));
        assertTrue(output.contains("Tasks finished: 1"));
        assertTrue(output.contains("You keep skipping @errand"));
        assertTrue(output.contains("Restored the task you completed"));
        assertTrue(output.contains("Nothing to undo."));
        assertTrue(output.contains("Done pile cleared."));
        assertEquals(2, app.countOpen());
    }

    @Test
    public void testRunNullArguments()
    {
        app.run(null, System.out);
        app.run(new Scanner(""), null);
        assertEquals(0, app.countOpen());
    }

    @Test
    public void testRunOnSystemIn()
    {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream("0\n".getBytes()));
            System.setOut(new PrintStream(bytes));
            app.run();
        }
        finally
        {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        assertTrue(bytes.toString().contains("Goodbye!"));
    }
}
