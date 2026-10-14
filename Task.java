/**
 * Creates the tasks the user adds, storing certain attributes: a
 * description, an estimate of how many minutes it takes (1 to 480), how
 * much energy it needs ("low" or "high"), and a chain of one or more
 * labels such as @laptop.
 *
 * A Task can never be created in an invalid state: the constructor
 * rejects bad values, so even if the app has a bug, no bad Task exists.
 *
 * @author Ashna, Prateek
 * @version October 2026
 */
public class Task
{
    /** Shortest allowed estimate, in minutes. */
    public static final int MIN_MINUTES = 1;
    /** Longest allowed estimate, in minutes (8 hours). */
    public static final int MAX_MINUTES = 480;
    /** Longest allowed description, in characters. */
    public static final int MAX_DESCRIPTION = 200;

    private String description;
    private int minutes;
    private String energy;
    private LinkedChain<Label> labels;

    /**
     * Creates a new task. The given labels are copied into the task's own
     * chain, skipping duplicates, so later changes to the caller's chain
     * do not affect this task.
     *
     * @param description what the task is; must not be blank
     * @param minutes     how long it takes, 1 to 480
     * @param energy      "low" or "high" (any case, surrounding spaces ok)
     * @param labels      the labels for the task; at least one
     * @throws IllegalArgumentException if any value is invalid or there
     *         are no labels
     */
    public Task(
        String description,
        int minutes,
        String energy,
        LinkedChain<Label> labels)
    {
        if (description == null || description.trim().isEmpty())
        {
            throw new IllegalArgumentException(
                "Description cannot be empty.");
        }
        if (description.trim().length() > MAX_DESCRIPTION)
        {
            throw new IllegalArgumentException(
                "Description cannot be longer than " + MAX_DESCRIPTION
                + " characters.");
        }
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES)
        {
            throw new IllegalArgumentException(
                "Minutes must be between " + MIN_MINUTES + " and "
                + MAX_MINUTES + ".");
        }
        if (!isValidEnergy(energy))
        {
            throw new IllegalArgumentException(
                "Energy must be either low or high.");
        }
        if (labels == null || labels.isEmpty())
        {
            throw new IllegalArgumentException(
                "A task needs at least one label.");
        }
        this.description = description.trim();
        this.minutes = minutes;
        this.energy = energy.trim().toLowerCase();
        this.labels = new LinkedChain<Label>();
        Label[] given = labels.toArray(new Label[labels.size()]);
        for (Label label : given)
        {
            if (!this.labels.contains(label))
            {
                this.labels.addToEnd(label);
            }
        }
    }

    /**
     * Checks whether a string is an allowed energy level.
     *
     * @param energy the text to check
     * @return true if energy is "low" or "high", ignoring case and
     *         surrounding spaces
     */
    public static boolean isValidEnergy(String energy)
    {
        if (energy == null)
        {
            return false;
        }
        String e = energy.trim().toLowerCase();
        return e.equals("low") || e.equals("high");
    }

    /**
     * @return what the task is
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return how many minutes the task takes
     */
    public int getMinutes()
    {
        return minutes;
    }

    /**
     * @return "low" or "high"
     */
    public String getEnergy()
    {
        return energy;
    }

    /**
     * Gets the chain of labels on this task. The chain belongs to the
     * task; use addLabel and removeLabel to change it.
     *
     * @return the labels on this task
     */
    public LinkedChain<Label> getLabels()
    {
        return labels;
    }

    /**
     * Checks whether this task carries a label.
     *
     * @param label the label to look for
     * @return true if the task has the label; false if not or if null
     */
    public boolean hasLabel(Label label)
    {
        return label != null && labels.contains(label);
    }

    /**
     * Adds a label to this task. A task holds each label at most once.
     *
     * @param newLabel the label to add
     * @return true if it was added; false if null or already on the task
     */
    public boolean addLabel(Label newLabel)
    {
        if (newLabel == null || labels.contains(newLabel))
        {
            return false;
        }
        labels.addToEnd(newLabel);
        return true;
    }

    /**
     * Removes a label from this task. A task always keeps at least one
     * label, so the last label cannot be removed.
     *
     * @param oldLabel the label to remove
     * @return true if it was removed; false if null, not on the task, or
     *         the task's only label
     */
    public boolean removeLabel(Label oldLabel)
    {
        if (oldLabel == null || labels.size() <= 1)
        {
            return false;
        }
        return labels.remove(oldLabel);
    }

    /**
     * Two tasks are equal when description (ignoring case), minutes,
     * energy, and the set of labels all match. Label order does not
     * matter.
     *
     * @param o the object to compare with
     * @return true if o is an equal Task
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
        Task other = (Task)o;
        return description.equalsIgnoreCase(other.getDescription())
            && minutes == other.getMinutes()
            && energy.equals(other.getEnergy())
            && sameLabels(other.getLabels());
    }

    /**
     * Compares this task's labels with another chain, ignoring order.
     * LinkedChain does not override equals, so this compares contents
     * through the chain's public methods.
     *
     * @param otherLabels the other task's labels
     * @return true if both chains hold the same labels
     */
    private boolean sameLabels(LinkedChain<Label> otherLabels)
    {
        if (labels.size() != otherLabels.size())
        {
            return false;
        }
        Label[] mine = labels.toArray(new Label[labels.size()]);
        for (Label label : mine)
        {
            if (labels.count(label) != otherLabels.count(label))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Hash code consistent with equals.
     *
     * @return a hash code for this task
     */
    @Override
    public int hashCode()
    {
        int hash = description.toLowerCase().hashCode();
        hash = 31 * hash + minutes;
        hash = 31 * hash + energy.hashCode();
        hash = 31 * hash + labels.size();
        return hash;
    }

    /**
     * Describes the task, e.g. "Email advisor (15 min, low) @laptop".
     *
     * @return a readable description of the task
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(description).append(" (").append(minutes)
            .append(" min, ").append(energy).append(")");
        Label[] mine = labels.toArray(new Label[labels.size()]);
        for (Label label : mine)
        {
            sb.append(" ").append(label);
        }
        return sb.toString();
    }
}
