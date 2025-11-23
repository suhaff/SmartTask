package smarttask;

public class Task {

    private String title;
    private String detail;
    private String category;
    private boolean completed;

    public Task() {}

    public Task(String title, String detail, String category, boolean completed) {
        this.title = title;
        this.detail = detail;
        this.category = category;
        this.completed = completed;
    }

    // Getters & setters
    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail == null ? "" : detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getCategory() { return category == null ? "General" : category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // JSON serialization
    public String toJson() {
        return "{\"title\":\"" + escape(getTitle()) +
                "\",\"detail\":\"" + escape(getDetail()) +
                "\",\"category\":\"" + escape(getCategory()) +
                "\",\"completed\":" + completed + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // JSON parsing (works with the crude format we write)
    public static Task fromJson(String json) {
        if (json == null) return new Task();
        String j = json.trim();
        if (j.startsWith("[")) j = j.substring(1);
        if (j.endsWith("]")) j = j.substring(0, j.length() - 1);
        if (j.startsWith("{") && j.endsWith("}")) j = j.substring(1, j.length() - 1);

        String title = "";
        String detail = "";
        String category = "General";
        boolean completed = false;

        // split on top-level commas; but since our fields are simple this is fine:
        String[] parts = j.split("\",\"");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].replaceAll("^\"|\"$", "");
            // handle key:value pairs
            String[] kv = p.split("\":");
            if (kv.length < 2) {
                // try alternative split
                int idx = p.indexOf("\":");
                if (idx >= 0) kv = new String[]{p.substring(0, idx), p.substring(idx + 2)};
            }
            if (kv.length < 2) continue;
            String key = kv[0].replaceAll("^\"|\"$", "").replaceAll("^,","").trim();
            String val = kv[1].trim();
            val = val.replaceAll("^\"|\"$", ""); // strip quotes

            switch (key) {
                case "title": title = val; break;
                case "detail": detail = val; break;
                case "category": category = val; break;
                case "completed":
                    completed = "true".equalsIgnoreCase(val) || val.equals("1");
                    break;
            }
        }

        return new Task(title, detail, category, completed);
    }
}
