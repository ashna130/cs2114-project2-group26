import java.util.Objects;

public class Task {
    private String description;
    private int minutes;
    private String energy;
    private LinkedChain<Label> labels;

    public Task(String description, int minutes, String energy, LinkedChain<Label> labels) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (minutes < 1 || minutes > 480) {
            throw new IllegalArgumentException("Minutes must be between 1 and 480.");
        }
        if (energy == null || !energy.toLowerCase().equals("low") || !energy.toLowerCase().equals("high")) {
            throw new IllegalArgumentException("Energy must be either low or high.");
        }
        this.description = description;
        this.minutes = minutes;
        this.energy = engergy.toLowerCase();
        this.labels = labels;
    }
}