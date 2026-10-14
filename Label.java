/**
 * Describes the task with a tag that says where or how it can be done,
 * such as @laptop, @phone, or @errand. Labels are normalized when
 * created: surrounding whitespace and leading '@' signs are removed and
 * the name is lower-cased, so " @Laptop " and "laptop" are the same label.
 *
 * @author Ashna, Prateek
 * @version October 14, 2026
 */
public class Label
{
    /** The longest label name allowed, in characters. */
    public static final int MAX_LENGTH = 30;

    private String name;

    /**
     * Creates a new label.
     *
     * @param name the label text, with or without a leading '@'
     * @throws IllegalArgumentException if name is null, blank, only "@",
     *         contains spaces, or is longer than MAX_LENGTH
     */
    public Label(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Label cannot be empty.");
        }
        String cleaned = name.trim();
        while (cleaned.startsWith("@"))
        {
            cleaned = cleaned.substring(1).trim();
        }
        if (cleaned.isEmpty())
        {
            throw new IllegalArgumentException("Label cannot be empty.");
        }
        if (cleaned.length() > MAX_LENGTH)
        {
            throw new IllegalArgumentException(
                "Label cannot be longer than " + MAX_LENGTH
                + " characters.");
        }
        if (cleaned.contains(" "))
        {
            throw new IllegalArgumentException(
                "Label cannot contain spaces.");
        }
        this.name = cleaned.toLowerCase();
    }

    /**
     * Gets the normalized name of this label, without the '@'.
     *
     * @return the label name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Two labels are equal when their normalized names match.
     *
     * @param o the object to compare with
     * @return true if o is a Label with the same name
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
        Label other = (Label)o;
        return name.equals(other.getName());
    }

    /**
     * Returns the label as the user types it, e.g. "@laptop".
     *
     * @return the label with a leading '@'
     */
    @Override
    public String toString()
    {
        return "@" + name;
    }
}
