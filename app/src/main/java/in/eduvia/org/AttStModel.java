package in.eduvia.org;

public class AttStModel {
    private int id;
    private String name;
    private String avatar;
    private String status; // "present", "absent", or "" (empty)

    public AttStModel(int id, String name, String avatar, String status) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.status = status != null ? status.toLowerCase() : ""; // normalize
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status != null ? status.toLowerCase() : "";
    }

    // Convenience methods for UI
    public boolean isPresent() {
        return "present".equalsIgnoreCase(status);
    }

    public boolean isAbsent() {
        return "absent".equalsIgnoreCase(status);
    }

    public boolean isEmpty() {
        return status == null || status.isEmpty();
    }
}
