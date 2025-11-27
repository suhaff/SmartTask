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

    // ---------------------------
    // Getters + Setters
    // ---------------------------
    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail == null ? "" : detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getCategory() { return category == null ? "General" : category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }


    // ---------------------------
    // JSON SERIALIZATION
    // ---------------------------
    public String toJson() {
        return "{"
                + "\"title\":\"" + escape(getTitle()) + "\","
                + "\"detail\":\"" + escape(getDetail()) + "\","
                + "\"category\":\"" + escape(getCategory()) + "\","
                + "\"completed\":" + completed
                + "}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ---------------------------
    // JSON PARSING
    // ---------------------------
    public static Task fromJson(String json) {
        if (json == null || json.isEmpty()) return new Task();

        String raw = json.trim();
        if (raw.startsWith("{")) raw = raw.substring(1);
        if (raw.endsWith("}")) raw = raw.substring(0, raw.length() - 1);

        String title = "";
        String detail = "";
        String category = "General";
        boolean completed = false;

        // Split by top-level commas
        String[] fields = raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String item : fields) {
            String[] kv = item.split(":", 2);
            if (kv.length < 2) continue;

            String key = kv[0].replace("\"","").trim();
            String val = kv[1].trim().replaceAll("^\"|\"$", "");

            switch (key) {
                case "title" -> title = val;
                case "detail" -> detail = val;
                case "category" -> category = val;
                case "completed" -> completed = val.equals("true");
            }
        }

        return new Task(title, detail, category, completed);
    }
}
