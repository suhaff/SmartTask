package smarttask;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    @FXML private VBox cardContainer;

    // menu items
    @FXML private MenuItem menuSave;
    @FXML private MenuItem menuLoad;
    @FXML private MenuItem menuExit;
    @FXML private MenuItem menuAbout;

    private boolean dark = false;
    private File dataFile = new File("tasks.json");
    private final List<Task> tasks = new ArrayList<>();

    @FXML
    public void initialize() {

        // Populate category list
        filterCategory.getItems().addAll("All", "General", "Work", "Study", "Personal");
        filterCategory.getSelectionModel().select("All");

        // Listeners
        addButton.setOnAction(e -> openAddDialog());
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterCategory.valueProperty().addListener((obs, o, n) -> applyFilters());
        themeToggle.setOnAction(e -> toggleTheme());

        // Menu listeners
        menuSave.setOnAction(e -> saveAs());
        menuLoad.setOnAction(e -> loadFromFile());
        menuExit.setOnAction(e -> System.exit(0));
        menuAbout.setOnAction(e -> showAbout());

        loadTasksFromDisk();
        applyFilters();
    }

    private Stage getStage() {
        return (Stage) taskList.getScene().getWindow();
    }

    // ----------------------------------------------------------
    // ABOUT
    // ----------------------------------------------------------
    private void showAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About SmartTask");
        a.setHeaderText("SmartTask - JavaFX Todo Application");
        a.setContentText("Created by:\n- ARUNPILLAI A/L RAGHAVAN\n- HERISS RAJ A/L RAVI\n- SHARVVESH A/L SUKUMARAN\n\nFeatures:\n✔ Dark Mode\n✔ Save/Load JSON\n✔ Clean UI\n✔ Responsive Layout");
        a.showAndWait();
    }

    // ----------------------------------------------------------
    // SAVE AS
    // ----------------------------------------------------------
    private void saveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Tasks");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("tasks.json");

        File picked = chooser.showSaveDialog(getStage());
        if (picked != null) {
            dataFile = picked;
            saveTasksToDisk();
        }
    }

    // ----------------------------------------------------------
    // LOAD FILE
    // ----------------------------------------------------------
    private void loadFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Tasks");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File picked = chooser.showOpenDialog(getStage());
        if (picked != null) {
            dataFile = picked;
            tasks.clear();
            loadTasksFromDisk();
            applyFilters();
        }
    }

    // ----------------------------------------------------------
    // ADD TASK
    // ----------------------------------------------------------
    private void openAddDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Create Task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField();
        title.setPromptText("Task title");

        TextArea detail = new TextArea();
        detail.setPromptText("Details...");
        detail.setPrefRowCount(3);

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("General", "Work", "Study", "Personal");
        category.setValue("General");

        VBox box = new VBox(8,
                new Label("Title:"), title,
                new Label("Details:"), detail,
                new Label("Category:"), category
        );

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !title.getText().trim().isEmpty()) {
                return new Task(title.getText().trim(), detail.getText().trim(), category.getValue(), false);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            tasks.add(task);
            saveTasksToDisk();
            applyFilters();
        });
    }

    // ----------------------------------------------------------
    // EDIT TASK
    // ----------------------------------------------------------
    private void openEditDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField(task.getTitle());
        TextArea detail = new TextArea(task.getDetail());
        detail.setPrefRowCount(3);

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("General", "Work", "Study", "Personal");
        category.setValue(task.getCategory());

        VBox box = new VBox(8,
                new Label("Title:"), title,
                new Label("Details:"), detail,
                new Label("Category:"), category
        );

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                task.setTitle(title.getText().trim());
                task.setDetail(detail.getText().trim());
                task.setCategory(category.getValue());
                return task;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            saveTasksToDisk();
            applyFilters();
        });
    }

    // ----------------------------------------------------------
    // VIEW TASK
    // ----------------------------------------------------------
    private void openViewDialog(Task task) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Task Details");
        a.setHeaderText(task.getTitle());
        a.setContentText(
            "Category: " + task.getCategory() + "\n\n" +
            (task.getDetail().isEmpty() ? "(No details)" : task.getDetail())
        );
        a.showAndWait();
    }

    // ----------------------------------------------------------
    // ADD ROW TO UI
    // ----------------------------------------------------------
    private void addTaskRow(Task task) {

        HBox row = new HBox(12);
        row.setStyle("-fx-padding:10; -fx-background-color:rgba(0,0,0,0.03); -fx-background-radius:6;");

        CheckBox cb = new CheckBox();
        cb.setSelected(task.isCompleted());
        cb.selectedProperty().addListener((obs, o, n) -> task.setCompleted(n));

        TextField txt = new TextField(task.getTitle());
        txt.setStyle("-fx-background-color:transparent; -fx-border-width:0; -fx-font-size:16;");
        HBox.setHgrow(txt, Priority.ALWAYS);
        txt.textProperty().addListener((obs, o, n) -> task.setTitle(n));

        Button view = new Button("View");
        view.setOnAction(e -> openViewDialog(task));

        Button edit = new Button("Edit");
        edit.setOnAction(e -> openEditDialog(task));

        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    tasks.remove(task);
                    saveTasksToDisk();
                    applyFilters();
                }
            });
        });

        row.getChildren().addAll(cb, txt, view, edit, delete);
        taskList.getChildren().add(row);
    }

    // ----------------------------------------------------------
    // FILTER
    // ----------------------------------------------------------
    private void applyFilters() {
        taskList.getChildren().clear();
        String search = searchField.getText().toLowerCase();
        String cat = filterCategory.getValue();

        for (Task t : tasks) {
            boolean match =
                t.getTitle().toLowerCase().contains(search) &&
                (cat.equals("All") || t.getCategory().equalsIgnoreCase(cat));

            if (match) addTaskRow(t);
        }
    }

    // ----------------------------------------------------------
    // THEME
    // ----------------------------------------------------------
    private void toggleTheme() {
        dark = !dark;

        Node root = taskList.getScene().getRoot();

        if (dark) {
            themeToggle.setText("☀️");
            root.setStyle("-fx-background-color:#1e1e1e;");
            cardContainer.setStyle("-fx-background-color:#2b2b2b; -fx-background-radius:20;");

            searchField.setStyle("-fx-background-color:#3a3a3a; -fx-text-fill:white;");
            filterCategory.setStyle("-fx-background-color:#3a3a3a; -fx-text-fill:white;");

            for (var n : taskList.getChildren()) {
                if (n instanceof HBox row) {
                    row.setStyle("-fx-padding:10; -fx-background-color:#3a3a3a; -fx-background-radius:6;");
                    for (var c : row.getChildren()) {
                        if (c instanceof TextField tf)
                            tf.setStyle("-fx-background-color:transparent; -fx-text-fill:white;");
                        if (c instanceof Button btn)
                            btn.setStyle("-fx-background-color:#555; -fx-text-fill:white;");
                    }
                }
            }

        } else {
            themeToggle.setText("🌙");
            root.setStyle("-fx-background-color:#D6E7FF;");
            cardContainer.setStyle("-fx-background-color:white; -fx-background-radius:20;");

            searchField.setStyle("");
            filterCategory.setStyle("");

            for (var n : taskList.getChildren()) {
                if (n instanceof HBox row) {
                    row.setStyle("-fx-padding:10; -fx-background-color:rgba(0,0,0,0.03);");
                    for (var c : row.getChildren()) {
                        if (c instanceof TextField tf)
                            tf.setStyle("-fx-background-color:transparent; -fx-text-fill:black;");
                        if (c instanceof Button btn)
                            btn.setStyle("");
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------
    // SAVE TO DISK
    // ----------------------------------------------------------
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

    // ----------------------------------------------------------
    // LOAD FROM DISK (PHANTOM TASKS FIXED)
    // ----------------------------------------------------------
    private void loadTasksFromDisk() {
        tasks.clear();

        if (!dataFile.exists()) return;

        try {
            String json = new String(Files.readAllBytes(dataFile.toPath())).trim();
            if (json.length() < 2) return;

            json = json.substring(1, json.length() - 1).trim(); // remove [ ]

            List<String> objects = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int depth = 0;

            for (char c : json.toCharArray()) {
                if (c == '{') {
                    if (depth == 0) current.setLength(0);
                    depth++;
                }

                if (depth > 0) current.append(c);

                if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String obj = current.toString().trim();
                        if (obj.startsWith("{") && obj.endsWith("}")) {
                            objects.add(obj);
                        }
                    }
                }
            }

            // Convert to tasks
            for (String obj : objects) {
                Task t = Task.fromJson(obj);
                if (!t.getTitle().trim().isEmpty()) {
                    tasks.add(t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
