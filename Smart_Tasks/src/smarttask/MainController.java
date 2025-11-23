package smarttask;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategory;
    @FXML private Button themeToggle;
    @FXML private VBox taskList;
    @FXML private Button addButton;

    private boolean dark = false;
    private final File dataFile = new File("tasks.json");

    // in-memory list of tasks
    private final List<Task> tasks = new ArrayList<>();

    @FXML
    public void initialize() {
        // populate filter
        filterCategory.getItems().addAll("All", "General", "Work", "Study", "Personal");
        filterCategory.getSelectionModel().select("All");

        // listeners
        addButton.setOnAction(e -> openAddDialog());
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCategory.valueProperty().addListener((obs, o, n) -> applyFilters());
        themeToggle.setOnAction(e -> toggleTheme());

        // load saved tasks
        loadTasksFromDisk();

        // initial render
        applyFilters();
    }

    // -----------------------------
    // Add / Edit / View / Delete
    // -----------------------------
    private void openAddDialog() {
        Dialog<Task> dlg = new Dialog<>();
        dlg.setTitle("Create Task");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField();
        title.setPromptText("Task title");
        TextArea detail = new TextArea();
        detail.setPromptText("Details (optional)");
        detail.setPrefRowCount(3);
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("General", "Work", "Study", "Personal");
        category.setValue("General");

        VBox v = new VBox(8,
                new Label("Title:"), title,
                new Label("Details:"), detail,
                new Label("Category:"), category);
        dlg.getDialogPane().setContent(v);

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String t = title.getText().trim();
                if (t.isEmpty()) return null;
                return new Task(t, detail.getText().trim(), category.getValue(), false);
            }
            return null;
        });

        Optional<Task> res = dlg.showAndWait();
        res.ifPresent(task -> {
            tasks.add(task);
            saveTasksToDisk();
            applyFilters();
        });
    }

    private void openEditDialog(Task task) {
        Dialog<Task> dlg = new Dialog<>();
        dlg.setTitle("Edit Task");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField(task.getTitle());
        TextArea detail = new TextArea(task.getDetail());
        detail.setPrefRowCount(3);
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("General", "Work", "Study", "Personal");
        category.setValue(task.getCategory() == null ? "General" : task.getCategory());

        VBox v = new VBox(8,
                new Label("Title:"), title,
                new Label("Details:"), detail,
                new Label("Category:"), category);
        dlg.getDialogPane().setContent(v);

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String t = title.getText().trim();
                if (t.isEmpty()) return null;
                task.setTitle(t);
                task.setDetail(detail.getText().trim());
                task.setCategory(category.getValue());
                return task;
            }
            return null;
        });

        Optional<Task> res = dlg.showAndWait();
        res.ifPresent(t -> {
            saveTasksToDisk();
            applyFilters();
        });
    }

    private void openViewDialog(Task task) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("View Task");
        a.setHeaderText(task.getTitle());
        StringBuilder sb = new StringBuilder();
        sb.append("Category: ").append(task.getCategory() == null ? "General" : task.getCategory()).append("\n\n");
        sb.append(task.getDetail() == null || task.getDetail().isEmpty() ? "(no details)" : task.getDetail());
        a.setContentText(sb.toString());
        a.showAndWait();
    }

    // -----------------------------
    // UI Row creation
    // -----------------------------
    private void addTaskRow(Task task) {
        HBox row = new HBox(12);
        row.setStyle("-fx-padding:10; -fx-background-color: rgba(0,0,0,0.03); -fx-background-radius:6;");
        row.setMinHeight(48);

        CheckBox cb = new CheckBox();
        cb.setSelected(task.isCompleted());
        cb.selectedProperty().addListener((obs, o, n) -> {
            task.setCompleted(n);
            saveTasksToDisk();
        });

        TextField titleField = new TextField(task.getTitle());
        titleField.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 16;");
        HBox.setHgrow(titleField, Priority.ALWAYS);
        titleField.textProperty().addListener((obs, o, n) -> {
            task.setTitle(n);
        });
        // Buttons
        Button viewBtn = new Button("View");
        viewBtn.setOnAction(e -> openViewDialog(task));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> openEditDialog(task));

        Button delBtn = new Button("Delete");
        delBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this task?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm delete");
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.YES) {
                tasks.remove(task);
                saveTasksToDisk();
                applyFilters();
            }
        });

        // compose row
        row.getChildren().addAll(cb, titleField, viewBtn, editBtn, delBtn);
        taskList.getChildren().add(row);
    }

    // -----------------------------
    // Filtering and rendering
    // -----------------------------
    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = filterCategory.getValue() == null ? "All" : filterCategory.getValue();

        taskList.getChildren().clear();

        for (Task t : tasks) {
            boolean searchMatch = t.getTitle() != null && t.getTitle().toLowerCase().contains(q);
            boolean categoryMatch = cat.equals("All") || (t.getCategory() != null && t.getCategory().equalsIgnoreCase(cat));
            if (searchMatch && categoryMatch) {
                addTaskRow(t);
            }
        }
    }

    // -----------------------------
    // Theme toggle
    // -----------------------------
    private void toggleTheme() {
        dark = !dark;
        Node root = taskList.getScene().getRoot();
        if (dark) {
            themeToggle.setText("☀️");
            root.setStyle("-fx-background-color:#1e1e1e; -fx-text-fill: white;");
        } else {
            themeToggle.setText("🌙");
            root.setStyle("-fx-background-color:#D6E7FF;");
        }
    }

    // -----------------------------
    // Save / Load JSON
    // -----------------------------
    public void saveTasksToDisk() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(dataFile))) {
            pw.println("[");
            for (int i = 0; i < tasks.size(); i++) {
                pw.print(tasks.get(i).toJson());
                if (i < tasks.size() - 1) pw.println(",");
            }
            pw.println("]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromDisk() {
        if (!dataFile.exists()) return;
        try {
            String json = new String(Files.readAllBytes(dataFile.toPath())).trim();
            if (json.length() < 3) return;
            // remove [ ] and split safely
            String inner = json.substring(1, json.length() - 1).trim();
            if (inner.isEmpty()) return;

            // split objects: handle {"..."},{"..."} or single object
            List<String> objects = new ArrayList<>();
            int depth = 0;
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < inner.length(); i++) {
                char c = inner.charAt(i);
                cur.append(c);
                if (c == '{') depth++;
                else if (c == '}') depth--;
                if (depth == 0 && cur.length() > 0) {
                    objects.add(cur.toString().trim());
                    cur = new StringBuilder();
                }
            }

            for (String o : objects) {
                Task t = Task.fromJson(o);
                tasks.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
