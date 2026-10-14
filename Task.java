/**
 * Creates the tasks the user adds, storing certain attributes.
 */
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
        this.energy = energy.toLowerCase();
        this.labels = labels;
    }

    public String getDescription() {
        return description;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getEnergy() {
        return energy;
    }

    public LinkedChain<Label> getLabels() {
        return labels;
    }

    public boolean addLabel(Label newLabel) {
        if (newLabel == null || labels.contains(newLabel)) {
            return false;
        }
        labels.add(newLabel);
        return true;
    }

    public boolean removeLabel(Label newLabel) {
        if (newLabel == null || labels.size() <= 1 || !labels.contains(newLabel)) {
            return false;
        }
        labels.remove(newLabel);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!this.getClass().equals(o.getClass())) {
            return false;
        }
        Task other = (Task) o;
        if (!this.description.equals(other.getDescription()) 
            || this.minutes != other.getMinutes() 
            || !this.energy.equals(other.getEnergy())
            || !this.labels.equals(other.getLabels())) {
                return false;
        }
        return true;
    }

    
}