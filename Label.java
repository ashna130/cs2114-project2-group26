import java.util.Objects;

public class Label {
    private String name

    public Label(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Label cannot be empty.");
        }
        this.name = name.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass != o.getClass()) {
            return false;
        }
        Label other = (Label) o;
        return name.equals(other.getName());
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}