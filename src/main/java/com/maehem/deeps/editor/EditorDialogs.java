/*
    Licensed to the Apache Software Foundation (ASF) under one or more 
    contributor license agreements.  See the NOTICE file distributed with this
    work for additional information regarding copyright ownership.  The ASF 
    licenses this file to you under the Apache License, Version 2.0 
    (the "License"); you may not use this file except in compliance with the 
    License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the 
    License for the specific language governing permissions and limitations 
    under the License.
 */
package com.maehem.deeps.editor;

import static com.maehem.deeps.Deeps.log;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Zone;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class EditorDialogs {

    public static void projectSettingsDialog(EditorProject project, Stage stage) {
        // Present a dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Project Settings");
        dialog.setHeaderText("Edit project settings.");
        dialog.setResizable(true);
        dialog.initOwner(stage);

        Label nameLabel = new Label("Name: ");
        Label pathLabel = new Label("Path: ");

        // TODO:  List File Path information.
        // TODO:  List Zones information.
        TextField nameText = new TextField(project.getName());

        TextField pathText = new TextField(project.getFilePath());
        Button pathButton = createIconButton("Select Project Folder", "/icons/folder.png");
        pathButton.setPadding(new Insets(5));
        HBox pathBox = new HBox(pathText, pathButton);

        pathButton.setOnAction((t) -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File f = dirChooser.showDialog(stage);
            if (f != null) {
                File projectFile = new File(f, "project.properties");
                if (projectFile.exists()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText(
                            "The selected project directory is already in use "
                            + "by another project.  Clicking OK will delete the "
                            + "existing project information."
                    );
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        Alert doubleCheck = new Alert(Alert.AlertType.CONFIRMATION);
                        doubleCheck.setContentText(
                                "Replace previous project. Are "
                                + "you super sure? This cannot be undone!");
                        Optional<ButtonType> result2 = doubleCheck.showAndWait();
                        if (result2.get() == ButtonType.OK) {
                            log.severe("Replace Old Project!");
                            for (File deleteMe : f.listFiles()) {
                                deleteMe.delete();
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                pathText.setText(f.getAbsolutePath());
                File sheetsDir = new File(f, "sheets");
                sheetsDir.mkdir();
            }
        });

        GridPane grid = new GridPane();
        grid.setPrefWidth(400);
        grid.add(nameLabel, 1, 1);
        grid.add(nameText, 2, 1);
        grid.add(pathLabel, 1, 2);
        grid.add(pathBox, 2, 2);

        dialog.getDialogPane().setContent(grid);

        // TODO:  List zones with their own edit buttons.
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter((ButtonType p) -> {
            if (p.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!project.getName().equals(nameText.getText())) {
                    project.setName(nameText.getText());
                    project.setEdited(true);
                }
                if (!project.getFilePath().equals(pathText.getText())) {
                    project.setFilePath(new File(pathText.getText()));
                    project.setEdited(true);
                }
                project.doSave();

                return "OK";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

    }

    protected static void newZoneDialog(EditorProject project) {
        // Present a dialog.
        Dialog<Zone> dialog = new Dialog<>();
        dialog.setTitle("Create New Zone");
        dialog.setHeaderText("Create a new zone. \n"
                + "Enter info and press Okay (or click title bar 'X' for cancel).");
        dialog.setResizable(true);

        Label nameLabel = new Label("Name: ");
        Label widthLabel = new Label("Width: ");
        Label heightLabel = new Label("Height: ");
        TextField nameText = new TextField("Zone" + (int) (Math.random() * 10000));

        // TODO: Add code to only allow numeric entry.
        TextField widthText = new TextField(String.valueOf(Zone.WIDTH));
        TextField heightText = new TextField(String.valueOf(Zone.HEIGHT));

        GridPane grid = new GridPane();
        grid.add(nameLabel, 1, 1);
        grid.add(nameText, 2, 1);
        grid.add(widthLabel, 1, 2);
        grid.add(widthText, 2, 2);
        grid.add(heightLabel, 1, 3);
        grid.add(heightText, 2, 3);
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter((ButtonType b) -> {
            if (b == buttonTypeOk) {

                return new Zone(
                        project,
                        nameText.getText(),
                        Integer.valueOf(widthText.getText()),
                        Integer.valueOf(heightText.getText())
                );
            }

            return null;
        });

        Optional<Zone> result = dialog.showAndWait();

        if (result.isPresent()) {
            project.getZones().add(result.get());
            project.setEdited(true);
            project.notifyProjectChanged(project,EditorProjectListener.ChangeType.ZONE_NAME);
        }
    }

    public static void zoneSettingsDialog(EditorProject project, Zone zone) {
        // Present a dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zone Settings");
        dialog.setHeaderText("Edit zone settings.");
        dialog.setResizable(true);

        Label nameLabel = new Label("Name: ");
        Label widthLabel = new Label("Width: ");
        Label heightLabel = new Label("Height: ");

        // TODO: Add code to only allow numeric entry.
        TextField nameText = new TextField(zone.getName());
        TextField widthText = new TextField(Integer.toString(zone.getWidth()));
        TextField heightText = new TextField(Integer.toString(zone.getHeight()));

        GridPane grid = new GridPane();
        grid.add(nameLabel, 1, 1);
        grid.add(nameText, 2, 1);
        grid.add(widthLabel, 1, 2);
        grid.add(widthText, 2, 2);
        grid.add(heightLabel, 1, 3);
        grid.add(heightText, 2, 3);
        dialog.getDialogPane().setContent(grid);

        // TODO:  List sheets with their own edit buttons.
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {

                if (b == buttonTypeOk) {
                    if (!zone.getName().equals(nameText.getText())) {
                        zone.setName(nameText.getText());
                        project.setEdited(true);
                    }
                    Integer newWidth = Integer.valueOf(widthText.getText());
                    if (zone.getWidth() != newWidth) {
                        zone.setWidth(newWidth);
                        project.setEdited(true);
                    }
                    Integer newHeight = Integer.valueOf(heightText.getText());
                    if (zone.getHeight() != newHeight) {
                        zone.setHeight(newHeight);
                        project.setEdited(true);
                    }
                    if (project.isEdited()) {
                        project.notifyProjectChanged(
                                project,
                                EditorProjectListener.ChangeType.EDITED
                        );
                    }

                    return "OK";
                }

                return null; // User cancelled
            }
        });

        Optional<String> result = dialog.showAndWait();

//        if (result.isPresent()) {
//            //project.getZones().add(result.get());
//            
//            project.setEdited(true);
//            project.notifyProjectChanged();
//        }
    }

    private static Button createIconButton(String name, String path) {
        Image img = new Image(EditorDialogs.class.getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(16);
        view.setPreserveRatio(true);
        //Setting the size of the button
        //button.setPrefSize(80, 80);
        //Setting a graphic to the button

        Button b = new Button();
        b.setPadding(new Insets(2));
        b.setGraphic(view);
        b.setTooltip(new Tooltip(name));

        return b;
    }

//    protected static void oldSheetDialog(EditorProject project, Stage stage) {
//        // Dialog select sheet file  PNG
//        FileChooser sheetChooser = new FileChooser();
//        sheetChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("PNG Files", "*.png")
//        );
//        File newSheetFile = sheetChooser.showOpenDialog(stage);
//        if ( newSheetFile != null ) {
//            // Copy it to projectDir/sheets/...
//            File dest = new File(project.getSheetsDir(), newSheetFile.getName());
//            try {
//                Path copy = Files.copy(
//                        newSheetFile.toPath(),
//                        dest.toPath(),
//                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
//                        java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
//                        NOFOLLOW_LINKS
//                );
//                if ( copy != null ) {
//                    log.log(Level.WARNING, "Copied sheet to: {0}", copy.toString());
//                    
//                    //  Create the properties file.
//                    
//                    
//                    SheetModel sheet = new SheetModel(dest);
//                    project.getSheets().add(sheet);
//                    project.registerSheet(sheet);
//                    project.notifyProjectChanged();
//                }
//                
//            } catch (IOException ex) {
//                Logger.getLogger(EditorDialogs.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        
//        
//
//    }
    protected static void newSheetDialog(EditorProject project, Stage stage) {
        // Present a dialog.
        Dialog<SheetModel> dialog = new Dialog<>();
        dialog.setTitle("Create New Sheet");
        dialog.setHeaderText("Create a new sheet. \n"
                + "Enter info and press Okay (or click title bar 'X' for cancel).");
        dialog.setResizable(true);

        Label nameLabel = new Label("Name: ");
        Label uidLabel = new Label("UID: ");
        Label pathLabel = new Label("Path: ");
        //Label widthLabel = new Label("Width: ");
        //Label heightLabel = new Label("Height: ");
        Label sizeLabel = new Label("Tile Size: ");
        Label authorLabel = new Label("Author: ");

        // TODO: Add code to only allow numeric entry.
        TextField nameText = new TextField("Sheet" + (int) (Math.random() * 10000));
        TextField uidText = new TextField(String.valueOf((Math.random() * Long.MAX_VALUE) ));
        //TextField pathText = new TextField(project.getFilePath());
        TextField pathText = new TextField("");
        Button pathButton = createIconButton("Select Project Folder", "/icons/folder.png");
        pathButton.setPadding(new Insets(5));
        HBox pathBox = new HBox(pathText, pathButton);
        //TextField widthText = new TextField(String.valueOf(12));
        //TextField heightText = new TextField(String.valueOf(12));
        TextField sizeText = new TextField("16");
        TextField authorText = new TextField("Unknown");

        GridPane grid = new GridPane();
        
        grid.add(nameLabel, 1, 1);
        grid.add(nameText, 2, 1);

        grid.add(uidLabel, 1, 2);
        grid.add(uidText, 2, 2);

        grid.add(pathLabel, 1, 3);
        grid.add(pathBox, 2, 3);

        grid.add(sizeLabel, 1, 4);
        grid.add(sizeText, 2, 4);

        grid.add(authorLabel, 1, 5);
        grid.add(authorText, 2, 5);
        
        dialog.getDialogPane().setContent(grid);

        pathButton.setOnAction((ActionEvent t) -> {
            // Dialog select sheet file  PNG
            FileChooser sheetChooser = new FileChooser();
            sheetChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Files", "*.png")
            );
            File newSheetFile = sheetChooser.showOpenDialog(stage);
            if (newSheetFile != null) {
                pathText.setText(newSheetFile.getAbsolutePath());
            }
        });

        ButtonType buttonTypeOk = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter((ButtonType b) -> {
            if (b == buttonTypeOk) {
                String sheetPath = pathText.getText();
                if (!sheetPath.isBlank()) {
                    // Copy it to projectDir/sheets/...
                    File src = new File(sheetPath);
                    File dest = new File(project.getSheetsDir(), src.getName());
                    File propSrc = new File(
                            src.getParentFile(),
                            dest.getName().split(".png")[0] + ".properties"
                    );
                    File propDest = new File(
                            dest.getParentFile(),
                            dest.getName().split(".png")[0] + ".properties"
                    );
                    
                    try {
                        Path copy = Files.copy(
                                src.toPath(),
                                dest.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                                java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                                NOFOLLOW_LINKS
                        );
                        if (copy != null) {
                            log.log(Level.WARNING, "Copied sheet PNG to: {0}", copy.toString());
                            if ( propSrc.exists() ) { // Copy prop file to new location.
                                Path propCopy = Files.copy(
                                    propSrc.toPath(),
                                    propDest.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                                    java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                                    NOFOLLOW_LINKS
                                );
                                if ( propCopy != null ) {
                                    log.log(Level.INFO, 
                                            "Copied properties file for {0}", 
                                            copy.getFileName());
                                    return new SheetModel(dest);
                                }
                            }
//                            Properties sheetProps = new Properties();
//                            sheetProps.put("name", nameText.getText());
//                            sheetProps.put("size", sizeText.getText());
//                            sheetProps.put("author", authorText.getText());
//
//                            sheetProps.store(new FileOutputStream(propFile), "Created Props");
//                            log.log(Level.WARNING, "Created properties file for: {0}", propFile.getName());

                            //  Create the properties file.
                            //SheetModel sheet = new SheetModel(dest);
                            //project.getSheets().add(sheet);
                            //project.registerSheet(sheet);
                            //project.notifyProjectChanged();
                            //return new SheetModel(dest);
                            return SheetModel.createSheet(dest, 
                                    nameText.getText(),
                                    Long.valueOf(uidText.getText()),
                                    sizeText.getText(), 
                                    authorText.getText()
                            );
                        }

                    } catch (IOException ex) {
                        log.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }

            return null;
        });

        Optional<SheetModel> result = dialog.showAndWait();

        if (result.isPresent()) {
            project.getSheets().add(result.get());
            project.setEdited(true);
            project.notifyProjectChanged(
                    project, EditorProjectListener.ChangeType.EDITED
            );
        }
    }

    public static void sheetSettingsDialog(EditorProject project, SheetModel sheet) {
        // Present a dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sheet Settings");
        dialog.setHeaderText("Edit sheet settings.");
        dialog.setResizable(true);

        Label nameLabel = new Label("Name: ");
        Label widthLabel = new Label("Width: ");  // Read only
        Label heightLabel = new Label("Height: ");  // Read only
        Label sizeLabel = new Label("Tile Size: ");
        Label authorLabel = new Label("Author: ");

        // TODO: Add code to only allow numeric entry.
        TextField nameText = new TextField(sheet.getName());
        Label widthText = new Label(sheet.getWidth() + " tiles"); // Read only
        Label heightText = new Label(sheet.getHeight() + " tiles"); // Read Only
        TextField sizeText = new TextField(Integer.toString(sheet.getSize()));
        TextField authorText = new TextField(sheet.getAuthor());

        GridPane grid = new GridPane();
        grid.add(nameLabel, 1, 1);
        grid.add(nameText, 2, 1);
        grid.add(widthLabel, 1, 2);
        grid.add(widthText, 2, 2);
        grid.add(heightLabel, 1, 3);
        grid.add(heightText, 2, 3);
        grid.add(sizeLabel, 1, 4);
        grid.add(sizeText, 2, 4);
        grid.add(authorLabel, 1, 5);
        grid.add(authorText, 2, 5);
        dialog.getDialogPane().setContent(grid);

        // TODO:  List sheets with their own edit buttons.
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeDelete = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeDelete);

        dialog.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {

                if (b == buttonTypeOk) {
                    String newName = nameText.getText();
                    if (!sheet.getName().equals(newName)) {
                        sheet.setName(nameText.getText());
                        project.setEdited(true);
                    }
                    Integer newSize = Integer.valueOf(sizeText.getText());
                    if (sheet.getSize() != newSize) {
                        sheet.setSize(newSize);
                        project.setEdited(true);
                    }
                    String newAuthor = authorText.getText();
                    if (!sheet.getAuthor().equals(newAuthor)) {
                        sheet.setAuthor(newAuthor);
                        project.setEdited(true);
                    }
                    if (project.isEdited()) {
                        project.notifyProjectChanged(
                            project,EditorProjectListener.ChangeType.EDITED
                        );
                    }

                    return "OK";
                } else if (b == buttonTypeDelete) {
                    log.log(Level.INFO, "User selected Delete Sheet in Dialog.");
                    
                    Alert doubleCheck = new Alert(Alert.AlertType.CONFIRMATION);
                    doubleCheck.setContentText(
                            "Delete sheet '" + sheet.getName() + "'. Are "
                            + "you super sure? This cannot be undone!");
                    Optional<ButtonType> resultDeleteSheet = doubleCheck.showAndWait();
                    if (resultDeleteSheet.get() == ButtonType.OK) {
                        log.log(Level.SEVERE, "User deleted sheet ''{0}''", sheet.getName());
                        File sheetFile = new File(sheet.getPath());
                        sheetFile.delete();
                        // TODO:  Move File to backup.
                        
                        File sheetPropFile = new File(
                                sheetFile.getParentFile(), 
                                sheetFile.getName().split(".png")[0] + ".properties"
                        );
                        sheetPropFile.delete();
                        
                        // TODO: Move properties file to backup.
                        project.getSheets().remove(sheet);
                        
                        return "DELETE";
                    } else {
                        log.log(Level.INFO, "User cancelled Delete Sheet dialog.");
                        return null; // User cancelled
                    }
                }

                return null; // User cancelled
            }
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            project.setEdited(true);
            project.notifyProjectChanged(
                    project,EditorProjectListener.ChangeType.EDITED
            );
        }
        
    }

}
