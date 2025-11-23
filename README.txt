Smart Task - JavaFX Desktop Todo Application (Plain Java project)
----------------------------------------------------------------

Project name: smart task
Type: Plain Java project (no Maven). Use with IntelliJ IDEA or NetBeans + JavaFX SDK.

Files created in this package:
- src/smarttask/Main.java
- src/smarttask/MainController.java
- src/smarttask/Task.java
- resources/main.fxml
- resources/tasks.json (created at runtime)
- README.txt (this file)

How to open & run:
1) Install Java 17+ and JavaFX SDK (17 or 20). Download JavaFX SDK and note the lib folder.
2) Open IntelliJ IDEA:
   - Create a new project -> "Empty Project".
   - Copy the 'src' folder into your project root and the 'resources' folder into project root.
   - Mark 'resources' as Resources Root (right-click folder -> Mark Directory As -> Resources Root).
   - Add JavaFX library: File -> Project Structure -> Libraries -> + -> add the JavaFX SDK lib folder (all jars).
   - Run configuration:
     VM options: --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml

3) Or run from command line:
   javac -d out --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml src/smarttask/*.java
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp out smarttask.Main

SceneBuilder:
- The FXML file (resources/main.fxml) is compatible with SceneBuilder.
- Open main.fxml in SceneBuilder to visually edit the GUI.
- Record a 2-minute video showing you building the GUI starting from an empty scene and placing the left pane, controls, and table.
- The final GUI must be roughly similar to what you submit.

Assignment checklist (based on PDF):
- Add / Edit / Delete / Mark tasks: implemented.
- GUI built using SceneBuilder: FXML provided.
- Search & Filter: implemented (search box, category & status filters).
- Save / Load JSON: implemented (tasks.json saved in working directory).
- MenuBar: File (Export JSON, Exit), Help (About placeholder).

Notes on JSON:
- The app writes a simple JSON array to tasks.json. The parser is designed to read JSON produced by this app.

If you want, I can:
- Produce a ZIP of the full project (I created it here).
- Add a sample icon for the logo (left side).
- Add step-by-step video script for recording the GUI build.

------------