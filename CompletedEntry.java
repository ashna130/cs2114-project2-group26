import java.time.LocalDate;

/**
 * A task that has been finished, together with the day it was finished.
 * CompletedEntry objects live in the "done pile" and feed the weekly
 * summary.
 *
 * @author Prateek, Ashna
 * @version October 2026
 */
public class CompletedEntry
{
    private Task task;
    private LocalDate completedDate;

    /**
     * Creates a completed entry.
     *
     * @param task          the finished task; must not be null
     * @param completedDate the day it was finished; must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public CompletedEntry(Task task, LocalDate completedDate)
    {
        if (task == null)
        {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (completedDate == null)
        {
            throw new IllegalArgumentException(
                "Completed date cannot be null.");
        }
        this.task = task;
        this.completedDate = completedDate;
    }

    /**
     * @return the finished task
     */
    public Task getTask()
    {
        return task;
    }

    /**
     * @return the day the task was finished
     */
    public LocalDate getCompletedDate()
    {
        return completedDate;
    }

    /**
     * Two entries are equal when the task and the date both match.
     *
     * @param o the object to compare with
     * @return true if o is an equal CompletedEntry
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || this.getClass() != o.getClass())
        {
            return false;
        }
        CompletedEntry other = (CompletedEntry)o;
        return task.equals(other.getTask())
            && completedDate.equals(other.getCompletedDate());
    }

    /**
     * Hash code consistent with equals.
     *
     * @return a hash code for this entry
     */
    @Override
    public int hashCode()
    {
        return 31 * task.hashCode() + completedDate.hashCode();
    }

    /**
     * Describes the entry, e.g. "2026-10-14: Email advisor (15 min, low)".
     *
     * @return a readable description of the entry
     */
    @Override
    public String toString()
    {
        return completedDate + ": " + task;
    }
}
