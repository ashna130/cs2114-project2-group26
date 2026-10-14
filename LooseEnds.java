import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;

/**
 * Loose Ends: a to-do list for people who are good at writing tasks down
 * and bad at deciding what to do next. The user dumps tasks in, with a
 * time estimate, an energy level, and labels like @laptop or @phone. When
 * they have time, "What now?" (bag roulette) hands them one random open
 * task that fits their minutes, energy, and label. Finished tasks go to a
 * done pile that shows what was closed this week and which labels keep
 * getting skipped.
 *
 * LooseEnds owns three bags, all stored in the same generic LinkedChain:
 * <ul>
 * <li>openTasks: LinkedChain&lt;Task&gt;, the tasks still to do</li>
 * <li>labelBag: LinkedChain&lt;Label&gt;, one copy of a label for every
 * open task that carries it, so count(label) is how many open tasks
 * have that label</li>
 * <li>donePile: LinkedChain&lt;CompletedEntry&gt;, finished tasks</li>
 * </ul>
 * All application logic lives here; the chain is only used through its
 * public methods.
 *
 * Stretch goals implemented: the avoidance report and undo of the last
 * delete or complete.
 *
 * @author Prateek, Ashna
 * @version October 2026
 */
public class LooseEnds
{
    /** Longest menu input we bother to parse. */
    private static final int MAX_MENU_INPUT = 3;
    /** Typing this at any prompt inside an action goes back to the menu. */
    private static final String CANCEL = "cancel";

    private LinkedChain<Task> openTasks;
    private LinkedChain<Label> labelBag;
    private LinkedChain<CompletedEntry> donePile;

    private Random random;

    // The last "What now?" filters and pick, used by another().
    private boolean hasLastQuery;
    private int lastMinutes;
    private Label lastLabel;
    private String lastEnergy;
    private Task lastPick;

    // Undo (stretch goal): the last task deleted or completed.
    private Task undoTask;
    private CompletedEntry undoEntry; // null if the last action was delete

    /**
     * Creates an empty app with a random roulette.
     */
    public LooseEnds()
    {
        this(new Random());
    }

    /**
     * Creates an empty app that uses the given random generator for
     * roulette. Tests pass a seeded generator.
     *
     * @param random the random generator; null means a new Random
     */
    public LooseEnds(Random random)
    {
        openTasks = new LinkedChain<Task>();
        labelBag = new LinkedChain<Label>();
        donePile = new LinkedChain<CompletedEntry>();
        this.random = (random == null) ? new Random() : random;
        hasLastQuery = false;
    }

    // ------------------------------------------------------------------
    // Application logic
    // ------------------------------------------------------------------

    /**
     * Creates a task and adds it to the open bag, and adds each of its
     * labels to the label bag. Duplicate tasks are allowed (the menu asks
     * the user first).
     *
     * @param desc    what the task is
     * @param minutes how long it takes, 1 to 480
     * @param energy  "low" or "high"
     * @param labels  labels for the task; at least one
     * @return the new task, or null if any value was invalid
     */
    public Task addTask(
        String desc,
        int minutes,
        String energy,
        LinkedChain<Label> labels)
    {
        Task task;
        try
        {
            task = new Task(desc, minutes, energy, labels);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
        openTasks.add(task);
        addLabelsToBag(task);
        return task;
    }

    /**
     * Checks whether an open task equal to the given one already exists.
     * The menu uses this to ask "add anyway?".
     *
     * @param t the task to look for
     * @return true if an equal task is open; false if not or null
     */
    public boolean isOpen(Task t)
    {
        return t != null && openTasks.contains(t);
    }

    /**
     * Adds a label to an open task and to the label bag.
     *
     * @param t     an open task
     * @param label the label text, e.g. "@laptop"
     * @return true if added; false if the task is not open, the label is
     *         invalid, or the task already has it
     */
    public boolean addLabel(Task t, String label)
    {
        Label newLabel = makeLabel(label);
        if (newLabel == null || !isOpen(t))
        {
            return false;
        }
        if (!t.addLabel(newLabel))
        {
            return false;
        }
        labelBag.add(newLabel);
        return true;
    }

    /**
     * Removes a label from an open task and one copy from the label bag.
     *
     * @param t     an open task
     * @param label the label text, e.g. "@laptop"
     * @return true if removed; false if the task is not open, the label
     *         is invalid, the task does not have it, or it is the task's
     *         only label
     */
    public boolean removeLabel(Task t, String label)
    {
        Label oldLabel = makeLabel(label);
        if (oldLabel == null || !isOpen(t))
        {
            return false;
        }
        if (!t.removeLabel(oldLabel))
        {
            return false;
        }
        labelBag.remove(oldLabel);
        return true;
    }

    /**
     * Deletes an open task without completing it, and removes its labels
     * from the label bag. The task can be brought back with undo().
     *
     * @param t the task to delete
     * @return true if deleted; false if null or not open
     */
    public boolean deleteTask(Task t)
    {
        if (t == null || !openTasks.remove(t))
        {
            return false;
        }
        removeLabelsFromBag(t);
        undoTask = t;
        undoEntry = null;
        return true;
    }

    /**
     * Completes an open task today. See completeTask(Task, LocalDate).
     *
     * @param t the task to complete
     * @return true if completed; false if null or already gone
     */
    public boolean completeTask(Task t)
    {
        return completeTask(t, LocalDate.now());
    }

    /**
     * Completes an open task on a given day: removes it from the open bag
     * and its labels from the label bag, then adds a CompletedEntry to the
     * done pile. Nothing else happens unless the remove succeeds, so a
     * double-complete changes nothing.
     *
     * @param t    the task to complete
     * @param date the day it was completed
     * @return true if completed; false if t or date is null, or t is
     *         already gone
     */
    public boolean completeTask(Task t, LocalDate date)
    {
        if (t == null || date == null || !openTasks.remove(t))
        {
            return false;
        }
        removeLabelsFromBag(t);
        CompletedEntry entry = new CompletedEntry(t, date);
        donePile.add(entry);
        undoTask = t;
        undoEntry = entry;
        return true;
    }

    /**
     * Stretch goal: puts the most recently deleted or completed task back
     * into the open bag. A completed task's entry is taken back out of the
     * done pile. Only one action can be undone.
     *
     * @return true if a task was restored; false if there is nothing to
     *         undo, or the done pile was cleared since the task was
     *         completed
     */
    public boolean undo()
    {
        if (undoTask == null)
        {
            return false;
        }
        Task task = undoTask;
        CompletedEntry entry = undoEntry;
        undoTask = null;
        undoEntry = null;
        if (entry != null && !donePile.remove(entry))
        {
            return false;
        }
        openTasks.add(task);
        addLabelsToBag(task);
        return true;
    }

    /**
     * @return a description of what undo() would restore, or null if
     *         there is nothing to undo
     */
    public String undoDescription()
    {
        if (undoTask == null)
        {
            return null;
        }
        return (undoEntry == null ? "deleted: " : "completed: ") + undoTask;
    }

    /**
     * Bag roulette: picks a random open task that fits the time you have,
     * your energy, and (optionally) a label. Low energy only matches
     * low-energy tasks; high energy matches any task.
     *
     * @param minutes the minutes available, 1 to 480
     * @param label   the label the task must have, or null for any
     * @param energy  "low" or "high"
     * @return a random matching task, or null if none match or the input
     *         is invalid
     */
    public Task whatNow(int minutes, Label label, String energy)
    {
        if (!validFilter(minutes, energy))
        {
            return null;
        }
        hasLastQuery = true;
        lastMinutes = minutes;
        lastLabel = label;
        lastEnergy = energy.trim().toLowerCase();
        lastPick = pickMatch(null);
        return lastPick;
    }

    /**
     * "Give me a different one": picks another task using the same
     * filters as the last whatNow call, never repeating the task just
     * shown.
     *
     * @return another matching task, or null if there was no whatNow call
     *         yet or no other task matches
     */
    public Task another()
    {
        if (!hasLastQuery)
        {
            return null;
        }
        Task pick = pickMatch(lastPick);
        if (pick != null)
        {
            lastPick = pick;
        }
        return pick;
    }

    /**
     * When "What now?" finds nothing, works out which single filter rules
     * out the most open tasks, so the menu can offer to loosen it.
     *
     * @param minutes the minutes available
     * @param label   the label asked for, or null for any
     * @param energy  "low" or "high"
     * @return "time", "label", or "energy"; or null if the input is
     *         invalid or the bag is empty
     */
    public String mostRestrictiveFilter(int minutes, Label label, String energy)
    {
        if (!validFilter(minutes, energy) || openTasks.isEmpty())
        {
            return null;
        }
        boolean low = energy.trim().equalsIgnoreCase("low");
        int timeOk = 0;
        int labelOk = 0;
        int energyOk = 0;
        for (Task t : getOpenTasks())
        {
            if (t.getMinutes() <= minutes)
            {
                timeOk++;
            }
            if (label == null || t.hasLabel(label))
            {
                labelOk++;
            }
            if (!low || t.getEnergy().equals("low"))
            {
                energyOk++;
            }
        }
        if (labelOk <= timeOk && labelOk <= energyOk)
        {
            return "label";
        }
        return (timeOk <= energyOk) ? "time" : "energy";
    }

    /**
     * @return the number of open tasks
     */
    public int countOpen()
    {
        return openTasks.size();
    }

    /**
     * Counts how many open tasks carry a label.
     *
     * @param l the label
     * @return the count, or 0 if l is null
     */
    public int countLabel(Label l)
    {
        if (l == null)
        {
            return 0;
        }
        return labelBag.count(l);
    }

    /**
     * @return the number of completed entries in the done pile
     */
    public int countDone()
    {
        return donePile.size();
    }

    /**
     * Shows the done pile: how many tasks were closed in total and how
     * many under each label.
     *
     * @return the summary text, or "Nothing in the pile yet." if empty
     */
    public String doneSummary()
    {
        if (donePile.isEmpty())
        {
            return "Nothing in the pile yet.";
        }
        return "Done pile: " + donePile.size() + " task(s) closed in total"
            + labelCounts(getDonePile(), null, null);
    }

    /**
     * Summarizes the last 7 days (today and the 6 days before).
     *
     * @return the summary text
     */
    public String weeklySummary()
    {
        return weeklySummary(LocalDate.now());
    }

    /**
     * Summarizes the 7 days ending on a given day: how many tasks were
     * finished, the minutes spent, and a count per label.
     *
     * @param today the last day of the week to summarize
     * @return the summary text, "Nothing in the pile yet." if the done
     *         pile is empty, or a message if today is null
     */
    public String weeklySummary(LocalDate today)
    {
        if (today == null)
        {
            return "No date given.";
        }
        if (donePile.isEmpty())
        {
            return "Nothing in the pile yet.";
        }
        LocalDate start = today.minusDays(6);
        int finished = 0;
        int totalMinutes = 0;
        for (CompletedEntry entry : getDonePile())
        {
            if (inRange(entry.getCompletedDate(), start, today))
            {
                finished++;
                totalMinutes += entry.getTask().getMinutes();
            }
        }
        return "Week of " + start + " to " + today + "\n"
            + "Tasks finished: " + finished + "\n"
            + "Minutes spent: " + totalMinutes
            + labelCounts(getDonePile(), start, today);
    }

    /**
     * Stretch goal: finds the label the user keeps skipping. For each
     * label on an open task, compares how many open tasks have it with
     * how many finished tasks had it; the label with the biggest gap
     * (most open, fewest done) is the most avoided.
     *
     * @return the most avoided label, or null if there are no open tasks
     */
    public Label mostAvoidedLabel()
    {
        Label worst = null;
        int worstGap = Integer.MIN_VALUE;
        for (Label label : distinctLabels(labelBag))
        {
            int gap = labelBag.count(label) - doneCount(label);
            if (gap > worstGap)
            {
                worst = label;
                worstGap = gap;
            }
        }
        return worst;
    }

    /**
     * Stretch goal: the avoidance report shown in the menu.
     *
     * @return a sentence naming the most avoided label
     */
    public String avoidanceReport()
    {
        Label worst = mostAvoidedLabel();
        if (worst == null)
        {
            return "No open tasks, so nothing is being avoided.";
        }
        return "You keep skipping " + worst + ": "
            + labelBag.count(worst) + " open, " + doneCount(worst)
            + " done.";
    }

    /**
     * Empties the done pile. Open tasks are not touched.
     */
    public void clearDone()
    {
        donePile.clear();
    }

    /**
     * Gets the open tasks as an array, so the menu can number them.
     *
     * @return the open tasks; length 0 if there are none
     */
    public Task[] getOpenTasks()
    {
        return openTasks.toArray(new Task[openTasks.size()]);
    }

    /**
     * Gets the done pile as an array.
     *
     * @return the completed entries; length 0 if there are none
     */
    public CompletedEntry[] getDonePile()
    {
        return donePile.toArray(new CompletedEntry[donePile.size()]);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Makes a Label from user text.
     *
     * @param text the label text
     * @return the label, or null if the text is not a valid label
     */
    private Label makeLabel(String text)
    {
        try
        {
            return new Label(text);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Checks roulette filters.
     *
     * @param minutes the minutes
     * @param energy  the energy
     * @return true if both are valid
     */
    private boolean validFilter(int minutes, String energy)
    {
        return minutes >= Task.MIN_MINUTES && minutes <= Task.MAX_MINUTES
            && Task.isValidEnergy(energy);
    }

    /**
     * @param t a task
     * @return the task's labels as an array
     */
    private Label[] labelsOf(Task t)
    {
        LinkedChain<Label> labels = t.getLabels();
        return labels.toArray(new Label[labels.size()]);
    }

    /**
     * Adds one copy of each of a task's labels to the label bag.
     *
     * @param t the task entering the open bag
     */
    private void addLabelsToBag(Task t)
    {
        for (Label label : labelsOf(t))
        {
            labelBag.add(label);
        }
    }

    /**
     * Removes one copy of each of a task's labels from the label bag.
     *
     * @param t the task leaving the open bag
     */
    private void removeLabelsFromBag(Task t)
    {
        for (Label label : labelsOf(t))
        {
            labelBag.remove(label);
        }
    }

    /**
     * Lists each label in a chain once.
     *
     * @param labels a chain that may hold duplicates
     * @return the distinct labels, in chain order
     */
    private Label[] distinctLabels(LinkedChain<Label> labels)
    {
        LinkedChain<Label> distinct = new LinkedChain<Label>();
        for (Label label : labels.toArray(new Label[labels.size()]))
        {
            if (!distinct.contains(label))
            {
                distinct.addToEnd(label);
            }
        }
        return distinct.toArray(new Label[distinct.size()]);
    }

    /**
     * Counts finished tasks that had a label.
     *
     * @param label the label
     * @return how many done-pile entries carry it
     */
    private int doneCount(Label label)
    {
        int count = 0;
        for (CompletedEntry entry : getDonePile())
        {
            if (entry.getTask().hasLabel(label))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * @param d     a date
     * @param start first day, or null for no limit
     * @param end   last day, or null for no limit
     * @return true if d is between start and end, inclusive
     */
    private boolean inRange(LocalDate d, LocalDate start, LocalDate end)
    {
        return (start == null || !d.isBefore(start))
            && (end == null || !d.isAfter(end));
    }

    /**
     * Builds the "by label" lines for a set of finished entries.
     *
     * @param entries the done-pile entries
     * @param start   first day to include, or null
     * @param end     last day to include, or null
     * @return the lines, each starting with a newline
     */
    private String labelCounts(
        CompletedEntry[] entries,
        LocalDate start,
        LocalDate end)
    {
        LinkedChain<Label> used = new LinkedChain<Label>();
        for (CompletedEntry entry : entries)
        {
            if (inRange(entry.getCompletedDate(), start, end))
            {
                for (Label label : labelsOf(entry.getTask()))
                {
                    used.add(label);
                }
            }
        }
        if (used.isEmpty())
        {
            return "\nNo labels in this period.";
        }
        StringBuilder sb = new StringBuilder("\nBy label:");
        for (Label label : distinctLabels(used))
        {
            sb.append("\n  ").append(label).append(": ")
                .append(used.count(label));
        }
        return sb.toString();
    }

    /**
     * Checks a task against the last roulette filters.
     *
     * @param t the task
     * @return true if it fits
     */
    private boolean matches(Task t)
    {
        if (t.getMinutes() > lastMinutes)
        {
            return false;
        }
        if (lastEnergy.equals("low") && !t.getEnergy().equals("low"))
        {
            return false;
        }
        return lastLabel == null || t.hasLabel(lastLabel);
    }

    /**
     * Picks a random matching open task, skipping one task.
     *
     * @param skip a task to never pick (compared with equals), or null
     * @return a random match, or null if there is none
     */
    private Task pickMatch(Task skip)
    {
        LinkedChain<Task> candidates = new LinkedChain<Task>();
        for (Task t : getOpenTasks())
        {
            if (matches(t) && (skip == null || !t.equals(skip)))
            {
                candidates.add(t);
            }
        }
        if (candidates.isEmpty())
        {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    // ------------------------------------------------------------------
    // Text menu (the only part that talks to the user)
    // ------------------------------------------------------------------

    /**
     * Runs the menu on standard input and output.
     */
    public void run()
    {
        run(new Scanner(System.in), System.out);
    }

    /**
     * Runs the menu loop until the user quits or input ends (Ctrl-D).
     * Bad input is reported and re-prompted; it never crashes the loop.
     * Typing "cancel" inside any action goes back to the menu.
     *
     * @param in  where to read user input
     * @param out where to print
     */
    public void run(Scanner in, PrintStream out)
    {
        if (in == null || out == null)
        {
            return;
        }
        out.println("Welcome to Loose Ends!");
        out.println("(Type '" + CANCEL + "' at any prompt to go back.)");
        boolean running = true;
        while (running)
        {
            printMenu(out);
            String line = readLine(in, out, "Choose an option: ");
            if (line == null)
            {
                break;
            }
            try
            {
                running = handleChoice(parseMenuChoice(line), in, out);
            }
            catch (CancelException e)
            {
                out.println("Cancelled. Back to the menu.");
            }
            catch (EndOfInputException e)
            {
                running = false;
            }
        }
        out.println("Goodbye!");
    }

    /**
     * Prints the main menu.
     *
     * @param out where to print
     */
    private void printMenu(PrintStream out)
    {
        out.println();
        out.println("Open tasks: " + countOpen() + "   Done: " + countDone());
        out.println(" 1) Add a task");
        out.println(" 2) List open tasks");
        out.println(" 3) What now? (bag roulette)");
        out.println(" 4) Give me a different one");
        out.println(" 5) Complete a task");
        out.println(" 6) Delete a task");
        out.println(" 7) Add a label to a task");
        out.println(" 8) Remove a label from a task");
        out.println(" 9) Count open tasks with a label");
        out.println("10) Done pile and weekly summary");
        out.println("11) Clear the done pile");
        out.println("12) Avoidance report");
        out.println("13) Undo last delete/complete");
        out.println(" 0) Quit");
    }

    /**
     * Turns menu input into a choice number.
     *
     * @param line the trimmed input
     * @return the choice, or -1 if it is not a number or too long
     */
    private int parseMenuChoice(String line)
    {
        if (line.length() > MAX_MENU_INPUT)
        {
            return -1;
        }
        return parseIntOr(line, -1);
    }

    /**
     * Carries out one menu choice.
     *
     * @param choice the menu number
     * @param in     user input
     * @param out    where to print
     * @return false if the user chose to quit
     */
    private boolean handleChoice(int choice, Scanner in, PrintStream out)
    {
        switch (choice)
        {
            case 0:
                return false;
            case 1:
                menuAddTask(in, out);
                break;
            case 2:
                listOpen(out);
                break;
            case 3:
                menuWhatNow(in, out);
                break;
            case 4:
                showPick(another(), out, hasLastQuery
                    ? "No other task matches." : "Use What now? first.");
                break;
            case 5:
                menuRemove(in, out, true);
                break;
            case 6:
                menuRemove(in, out, false);
                break;
            case 7:
                menuChangeLabel(in, out, true);
                break;
            case 8:
                menuChangeLabel(in, out, false);
                break;
            case 9:
                menuCountLabel(in, out);
                break;
            case 10:
                out.println(doneSummary());
                if (!donePile.isEmpty())
                {
                    out.println();
                    out.println(weeklySummary());
                }
                break;
            case 11:
                clearDone();
                out.println("Done pile cleared.");
                break;
            case 12:
                out.println(avoidanceReport());
                break;
            case 13:
                menuUndo(out);
                break;
            default:
                out.println("Please enter a number from 0 to 13.");
        }
        return true;
    }

    /**
     * Asks for every task field, re-prompting on bad input while keeping
     * what was already typed.
     *
     * @param in  user input
     * @param out where to print
     */
    private void menuAddTask(Scanner in, PrintStream out)
    {
        String desc = promptDescription(in, out);
        int minutes = promptMinutes(in, out, "Minutes it takes (1-480): ");
        String energy = promptEnergy(in, out, "Energy (low/high): ");
        LinkedChain<Label> labels = promptLabels(in, out);

        if (isOpen(new Task(desc, minutes, energy, labels)))
        {
            String answer = mustRead(in, out,
                "You already have one that looks like this. "
                + "Add anyway? (y/n): ");
            if (!answer.toLowerCase().startsWith("y"))
            {
                out.println("Not added.");
                return;
            }
        }
        out.println("Added: " + addTask(desc, minutes, energy, labels));
    }

    /**
     * Runs bag roulette from the menu. If nothing matches, names the
     * most restrictive filter and offers to loosen it.
     *
     * @param in  user input
     * @param out where to print
     */
    private void menuWhatNow(Scanner in, PrintStream out)
    {
        if (openTasks.isEmpty())
        {
            out.println("Nothing in the pile yet. Add a task first!");
            return;
        }
        int minutes = promptMinutes(in, out,
            "How many minutes do you have (1-480)? ");
        String energy = promptEnergy(in, out, "Energy right now (low/high): ");
        Label label = null;
        while (true)
        {
            String text = mustRead(in, out,
                "Where are you / label (blank for any): ");
            if (text.isEmpty())
            {
                break;
            }
            label = makeLabel(text);
            if (label != null)
            {
                break;
            }
            out.println("That is not a valid label.");
        }

        Task pick = whatNow(minutes, label, energy);
        if (pick != null)
        {
            showPick(pick, out, null);
            return;
        }
        String tight = mostRestrictiveFilter(minutes, label, energy);
        out.println("Nothing fits right now. Your " + tight
            + " filter is the most restrictive.");
        String answer = mustRead(in, out, "Loosen it? (y/n): ");
        if (!answer.toLowerCase().startsWith("y"))
        {
            return;
        }
        if (tight.equals("time"))
        {
            minutes = Task.MAX_MINUTES;
        }
        else if (tight.equals("label"))
        {
            label = null;
        }
        else
        {
            energy = "high";
        }
        showPick(whatNow(minutes, label, energy), out,
            "Still nothing fits. Try adding more tasks.");
    }

    /**
     * Prints a roulette result.
     *
     * @param pick        the chosen task, or null
     * @param out         where to print
     * @param noneMessage what to say if pick is null
     */
    private void showPick(Task pick, PrintStream out, String noneMessage)
    {
        out.println(pick == null ? noneMessage : "How about: " + pick);
    }

    /**
     * Completes or deletes a task the user picks from the list.
     *
     * @param in       user input
     * @param out      where to print
     * @param complete true to complete, false to delete
     */
    private void menuRemove(Scanner in, PrintStream out, boolean complete)
    {
        Task t = chooseTask(in, out);
        if (t == null)
        {
            return;
        }
        boolean done = complete ? completeTask(t) : deleteTask(t);
        if (done)
        {
            out.println((complete ? "Nice! Completed: " : "Deleted: ") + t);
        }
        else
        {
            out.println("That task is already gone.");
            listOpen(out);
        }
    }

    /**
     * Adds or removes a label on a task the user picks.
     *
     * @param in  user input
     * @param out where to print
     * @param add true to add, false to remove
     */
    private void menuChangeLabel(Scanner in, PrintStream out, boolean add)
    {
        Task t = chooseTask(in, out);
        if (t == null)
        {
            return;
        }
        Label label = null;
        String text = "";
        while (label == null)
        {
            text = mustRead(in, out, "Label: ");
            label = makeLabel(text);
            if (label == null)
            {
                out.println("That is not a valid label (1-" + Label.MAX_LENGTH
                    + " characters, no spaces).");
            }
        }
        if (add)
        {
            out.println(addLabel(t, text) ? "Label added."
                : "That task already has " + label + ".");
        }
        else if (removeLabel(t, text))
        {
            out.println("Label removed.");
        }
        else if (t.hasLabel(label))
        {
            out.println("A task needs at least one label, so " + label
                + " stays.");
        }
        else
        {
            out.println("That task does not have " + label + ".");
        }
    }

    /**
     * Counts open tasks with a label the user types.
     *
     * @param in  user input
     * @param out where to print
     */
    private void menuCountLabel(Scanner in, PrintStream out)
    {
        while (true)
        {
            Label label = makeLabel(mustRead(in, out, "Label: "));
            if (label != null)
            {
                out.println(countLabel(label) + " open task(s) have "
                    + label + ".");
                return;
            }
            out.println("That is not a valid label.");
        }
    }

    /**
     * Undoes the last delete or complete from the menu.
     *
     * @param out where to print
     */
    private void menuUndo(PrintStream out)
    {
        String what = undoDescription();
        if (what == null)
        {
            out.println("Nothing to undo.");
        }
        else if (undo())
        {
            out.println("Restored the task you " + what);
        }
        else
        {
            out.println("Can't undo: the done pile was cleared.");
        }
    }

    /**
     * Prints the open tasks, numbered from 1.
     *
     * @param out where to print
     */
    private void listOpen(PrintStream out)
    {
        Task[] tasks = getOpenTasks();
        if (tasks.length == 0)
        {
            out.println("No open tasks. Nothing in the pile yet.");
            return;
        }
        for (int i = 0; i < tasks.length; i++)
        {
            out.println((i + 1) + ") " + tasks[i]);
        }
    }

    /**
     * Lets the user pick an open task by number.
     *
     * @param in  user input
     * @param out where to print
     * @return the chosen task, or null if there are none or the user
     *         cancelled with a blank line
     */
    private Task chooseTask(Scanner in, PrintStream out)
    {
        Task[] tasks = getOpenTasks();
        if (tasks.length == 0)
        {
            out.println("No open tasks. Nothing in the pile yet.");
            return null;
        }
        listOpen(out);
        while (true)
        {
            String text = mustRead(in, out,
                "Task number (blank to cancel): ");
            if (text.isEmpty())
            {
                return null;
            }
            int n = parseIntOr(text, -1);
            if (n >= 1 && n <= tasks.length)
            {
                return tasks[n - 1];
            }
            out.println("No task " + shorten(text) + ". Please enter a "
                + "number from 1 to " + tasks.length + ".");
            listOpen(out);
        }
    }

    /**
     * Asks for a description until it is not blank and not too long.
     *
     * @param in  user input
     * @param out where to print
     * @return the description
     */
    private String promptDescription(Scanner in, PrintStream out)
    {
        while (true)
        {
            String text = mustRead(in, out, "Description: ");
            if (text.isEmpty())
            {
                out.println("A task needs a description.");
            }
            else if (text.length() > Task.MAX_DESCRIPTION)
            {
                out.println("Description must be at most "
                    + Task.MAX_DESCRIPTION + " characters.");
            }
            else
            {
                return text;
            }
        }
    }

    /**
     * Asks for minutes until a whole number from 1 to 480 is typed.
     *
     * @param in     user input
     * @param out    where to print
     * @param prompt the prompt text
     * @return the minutes
     */
    private int promptMinutes(Scanner in, PrintStream out, String prompt)
    {
        while (true)
        {
            String text = mustRead(in, out, prompt);
            int n = parseIntOr(text, -1);
            if (n >= Task.MIN_MINUTES && n <= Task.MAX_MINUTES)
            {
                return n;
            }
            out.println("Please enter a whole number from "
                + Task.MIN_MINUTES + " to " + Task.MAX_MINUTES + ".");
        }
    }

    /**
     * Asks for "low" or "high" until one is typed.
     *
     * @param in     user input
     * @param out    where to print
     * @param prompt the prompt text
     * @return "low" or "high"
     */
    private String promptEnergy(Scanner in, PrintStream out, String prompt)
    {
        while (true)
        {
            String text = mustRead(in, out, prompt);
            if (Task.isValidEnergy(text))
            {
                return text.toLowerCase();
            }
            out.println("Please type low or high.");
        }
    }

    /**
     * Asks for labels separated by spaces or commas until at least one
     * valid label is typed. Bad labels are reported and skipped; the good
     * ones are kept.
     *
     * @param in  user input
     * @param out where to print
     * @return the labels typed (at least one)
     */
    private LinkedChain<Label> promptLabels(Scanner in, PrintStream out)
    {
        LinkedChain<Label> labels = new LinkedChain<Label>();
        while (labels.isEmpty())
        {
            String text = mustRead(in, out,
                "Labels, e.g. @laptop @phone (at least one): ");
            for (String part : text.split("[,\\s]+"))
            {
                if (part.isEmpty())
                {
                    continue;
                }
                Label label = makeLabel(part);
                if (label == null)
                {
                    out.println("Skipping bad label: " + shorten(part));
                }
                else if (!labels.contains(label))
                {
                    labels.addToEnd(label);
                }
            }
            if (labels.isEmpty())
            {
                out.println("A task needs at least one label.");
            }
        }
        return labels;
    }

    /**
     * Shortens very long text for messages.
     *
     * @param text the text
     * @return at most 40 characters of it
     */
    private String shorten(String text)
    {
        return text.length() <= 40 ? text : text.substring(0, 40) + "...";
    }

    /**
     * Parses a whole number.
     *
     * @param text     the text
     * @param fallback what to return if it is not a number
     * @return the number, or fallback
     */
    private int parseIntOr(String text, int fallback)
    {
        if (text == null || text.isEmpty() || text.length() > 9)
        {
            return fallback;
        }
        try
        {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Prints a prompt and reads one trimmed line.
     *
     * @param in     user input
     * @param out    where to print
     * @param prompt the prompt text
     * @return the trimmed line, or null at end of input
     */
    private String readLine(Scanner in, PrintStream out, String prompt)
    {
        out.print(prompt);
        if (!in.hasNextLine())
        {
            out.println();
            return null;
        }
        return in.nextLine().trim();
    }

    /**
     * Reads a line inside an action. End of input stops the program and
     * "cancel" goes back to the menu.
     *
     * @param in     user input
     * @param out    where to print
     * @param prompt the prompt text
     * @return the trimmed line
     * @throws EndOfInputException at end of input
     * @throws CancelException     if the user typed "cancel"
     */
    private String mustRead(Scanner in, PrintStream out, String prompt)
    {
        String line = readLine(in, out, prompt);
        if (line == null)
        {
            throw new EndOfInputException();
        }
        if (line.equalsIgnoreCase(CANCEL))
        {
            throw new CancelException();
        }
        return line;
    }

    /**
     * Signals that input ended (Ctrl-D) in the middle of an action.
     * Caught in run(), which then exits cleanly.
     */
    private static class EndOfInputException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Signals that the user typed "cancel". Caught in run(), which goes
     * back to the menu.
     */
    private static class CancelException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Starts the app.
     *
     * @param args not used
     */
    public static void main(String[] args)
    {
        new LooseEnds().run();
    }
}
