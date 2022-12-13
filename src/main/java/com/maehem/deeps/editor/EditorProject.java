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
import com.maehem.deeps.editor.EditorProjectListener.ChangeType;
import com.maehem.deeps.editor.EditorZoneEditor.Function;
import com.maehem.deeps.model.GameModel;
import com.maehem.deeps.model.SheetModel;
import com.maehem.deeps.model.Tile;
import com.maehem.deeps.model.ZoneFileFormatException;
import com.maehem.deeps.model.Zone;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public final class EditorProject implements GameModel {

    private static EditorProject INSTANCE;

    private static final String PREV_PROJECTS = "previous.projects";
    private final static String PROJECTS_DIR = "DeepsProjects"; // Under users ~/Documents/DeepsProjects folder
    public final static String PROJECTS_FILE = "project.properties"; // Under any projects dir,

    private String name = "Untitled";
    private File projectDir = null;
    private boolean edited = false;
    private boolean loaded = false;
    private final ArrayList<Zone> zones = new ArrayList<>();
    private final ArrayList<SheetModel> sheets = new ArrayList<>();

    private Stage stage;
    private Scene scene;

    private final ArrayList<EditorProjectListener> listeners = new ArrayList<>();
    private EditorZoneEditor.Function function;
    private long currentTileUID = -1L;

    private Tile currentSheetTile = null; // Tile that is highlighted in visible SheetViewTabPane
    private Tile focusedBaseTile = null; // Tile that is highlighted in visible Zone editor tab
    private Tile focusedItemTile = null; // Tile that is highlighted in visible Zone editor tab
    private Tile focusedEntityTile = null; // Tile that is highlighted in visible Zone editor tab

    private EditorProject() {
        File appProjectsDir = getProjectsDir();
        if (appProjectsDir.exists()) {
            log.log(Level.INFO,
                "Editor Projects Directory is: {0}", appProjectsDir
            );
        } else {
            if (appProjectsDir.mkdir()) {
                log.log(Level.INFO,
                    "Created Editor Projects Directory: {0}", appProjectsDir
                );
            } else {
                log.log(Level.SEVERE,
                    "Could not create projects directory: {0}",
                    appProjectsDir.getAbsolutePath()
                );
                return;
            }
        }
        
        File prevProjectsFile = new File(appProjectsDir, PREV_PROJECTS);
        if (!prevProjectsFile.exists()) {
            try {
                if (prevProjectsFile.createNewFile()) {
                    log.log(Level.INFO,
                            "Created previous projects file at: {0}",
                            appProjectsDir.getAbsolutePath()
                    );
                } else {
                    log.log(Level.SEVERE,
                            "Could not create a new previous projects file at: {0}",
                            prevProjectsFile.getAbsolutePath()
                    );
                }
            } catch (IOException ex) {
                Logger.getLogger(EditorProject.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.log(Level.INFO,
                    "Found previous projects file at: {0}",
                    appProjectsDir.getAbsolutePath()
            );            
        }

        ArrayList<String> projects = getPreviousProjectList();
        if ( !projects.isEmpty() ) {
            loadProject(new File(projects.get(0)));
        }
        
        notifyProjectChanged(this, ChangeType.LOADED);
    }

    public static EditorProject getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EditorProject();
        }

        return INSTANCE;
    }

    public boolean isLoaded() {
        return loaded;
    }
    
    public boolean isEdited() {
        return edited;
    }

    public void loadProject( File file ) {
            try {
                readFile( file );
                loaded = true;
                rememberPreviousProject(file);
                notifyProjectChanged(this, ChangeType.LOADED);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(EditorProject.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void clear() {
        // Prompt to save if project.edited
        if (isEdited()) {
            EditorDialogs.confirmCloseEditedProjectDialog(this, stage);
            // dialog calls project.clear() if user confirms.
        } else {
            setName("New Project");
            setFilePath(null);
            setEdited(false);
            setFocusedEntityTile(null);
            setFocusedFixtureTile(null);
            setFocusedMapTile(null);
            setCurrentSheetTile(-1L, null);
            setFunction(Function.SELECT);
            sheets.clear();
            zones.clear();
            loaded = false;
            notifyProjectChanged(this, ChangeType.CLEARED);
        }
    }
    
    public boolean addListener(EditorProjectListener l) {
        return listeners.add(l);
    }

    public boolean removeListener(EditorProjectListener l) {
        return listeners.remove(l);
    }
    
    public void setEdited(boolean state) {
        this.edited = state;
        notifyProjectChanged(this,ChangeType.EDITED);
    }

    protected void notifyProjectChanged(EditorProject p, ChangeType type) {
        if ( type == ChangeType.LOADED ) {
            loaded = true;
        }
        for (EditorProjectListener l : (ArrayList<EditorProjectListener>)listeners.clone()) {
            l.projectStateChanged(this,type);
        }
    }

    public void setStage( Stage stage ) {
        this.stage = stage;
    }
    
    public void setScene( Scene scene ) {
        this.scene = scene;
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getDir() {
        return projectDir;
    }

    public String getFilePath() {
        if (projectDir == null) {
            return "";
        }
        return projectDir.getAbsolutePath();
    }
    
    public long getCurrentTileUID() {
        return currentTileUID;
    }
    
    public File getSheetsDir() {
        return new File(projectDir, "sheets");
    }

    public void setFilePath(File dir) {
        projectDir = dir;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public List<SheetModel> getSheets() {
        return sheets;
    }

    @Override
    public SheetModel getDefaultSheet() {
        return sheets.get(0);
    }

    public void doSave() {
        if (projectDir == null) {
            log.severe("Save called on NULL project path!");
            return;
        }
        // save data
        writeFile();
        setEdited(false);
        notifyProjectChanged(this, ChangeType.SAVED);
    }

    private void writeFile() {
        // Save World Properties File
        Properties p = new Properties();
        p.put("name", getName());

        try {
            // Save Zone(s) information
            File propertiesFile = new File(projectDir, "project.properties");
            try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
                log.log(Level.INFO, "Store {0}", propertiesFile.getName());
                p.store(fos, "Deeps Project");
            }
            
            File zonesDir = new File(projectDir, "zones");
            if ( !zonesDir.exists() ) {
                log.log(Level.INFO, "zones directory didn't exist. Creating it now...");
                zonesDir.mkdir();
            }
            
            
            // Backup Zone files before writing new files.
            File backupZones = new File(zonesDir,"backups");
            backupZones.mkdir();
            for (File zf: getZoneFiles()) {
                File dest = new File(backupZones, 
                        zf.getName() + "_" + Instant.now().toString() 
                );
                if ( zf.renameTo(dest) ) {
                    zf.delete();
                } else {
                    log.log(Level.WARNING, 
                            "backup of file: {0} did not succeed.", 
                            zf.getName());
                }
            }
            
            for ( Zone z : getZones() ) {
                File zoneFile = new File(zonesDir, z.getName() + ".zone");
                log.log(Level.INFO, "Store Zone file: {0}", zoneFile.getName());
                try (FileOutputStream zos = new FileOutputStream(zoneFile)) {
                    z.store(zos);
                }
            }
            
            rememberPreviousProject(projectDir);
            
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    private void readFile(File projDir) throws FileNotFoundException {
        File propertiesFile = new File(projDir, "project.properties");
        Properties p = new Properties();
        try {
            FileInputStream is = new FileInputStream(propertiesFile);
            p.load(is);
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, "Project File Not Found", ex);
            throw ex;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Project File IO Exception", ex);
        }

        projectDir = projDir;
        setName(p.getProperty("name", "Unknown"));
        
        // Look in Sheets Dir and load those.
        File sheetsDir = new File(projDir, "sheets");
        File[] sheetPngs = sheetsDir.listFiles(
                (File dir, String name1) -> name1.endsWith(".png")
        );
        for ( File sheetFile: sheetPngs ) {
            try {
                SheetModel sm = new SheetModel(sheetFile); // Also loads the props file for sheet.
                addSheet(sm);
                //registerSheet(sm);
                
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        if ( !sheets.isEmpty() ) {
            setCurrentSheetTile(
                    sheets.get(0).getUID(), 
                    sheets.get(0).getTile(0) 
                    //new Tile(null,0, 0, 0, "")
            );
        }
        
        // Look in Zones Dir and load those.
        for ( File zoneFile: getZoneFiles() ) {
            try {
                Zone zm = Zone.load(this, new FileInputStream(zoneFile));
                log.log(Level.INFO, "Loaded Zone: {0}", zm.getName());
                getZones().add(zm);
            } catch (IOException ex) {
                log.log(Level.SEVERE, 
                        "IO Exception has occurred for Zone file: " + zoneFile.getName(), 
                        ex 
                );
            } catch (ZoneFileFormatException ex) {
                log.log(Level.SEVERE, 
                        "Zone File Load Failure! Zone file: " + zoneFile.getName(), 
                        ex
                );
            }
        }
    }

    private File[] getZoneFiles() {
        File zonesDir = new File(projectDir, "zones");
        return zonesDir.listFiles((File dir, String nam) -> nam.endsWith(".zone"));
    }

    public final File getProjectsDir() {
        String homeDirPath = System.getProperty("user.home");
        File homeDir = new File(homeDirPath);

        File documentsDir = new File(homeDir, "Documents");
        if (!documentsDir.isDirectory()) {
            log.log(Level.SEVERE, 
                    "Could not find User Documents directory!");
        }

        return new File(documentsDir, PROJECTS_DIR);
    }

    private File getPreviousProjectsListFile() {
        File projDir = getProjectsDir();
        return new File(projDir, PREV_PROJECTS);
    }
    
    private ArrayList<String> getPreviousProjectList() {
        ArrayList<String> lines = new ArrayList<>();
        
        BufferedReader reader;
        try {
            log.log(Level.CONFIG, "Read " + PREV_PROJECTS + " file...");
            reader = new BufferedReader(new FileReader(getPreviousProjectsListFile()));
            String line = reader.readLine();
            while (line != null) {
                log.log(Level.FINE, "    {0}", line);
                lines.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        if (lines.isEmpty()) {
            log.log(Level.INFO, "    no previous projects file found.");
        }

        return lines;
    }
    
    /**
     * Put project dir into list at element 0 
     * or move it up to 0 if it's already in the list.
     * 
     * @param proj project directory @File
     */
    private void rememberPreviousProject( File proj ) {
        String path = proj.getAbsolutePath();
        log.log(Level.INFO, "Remember project: {0}", path);
        File pFile = getPreviousProjectsListFile();
        ArrayList<String> list = getPreviousProjectList();
        list.remove(path);
        list.add(0, path);
        log.log(Level.CONFIG, "Write list of paths to file:");
        try (FileWriter fw = new FileWriter(pFile)) {
            for (String line : list) {
                log.log(Level.FINE, "    {0}", line);
                fw.append(line).append("\n");
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    protected void setFunction(Function function) {
        if ( this.function != function ) {
            this.function = function;
            notifyProjectChanged(this, ChangeType.FUNC);
        }
    }
    
    public Function getFunction() {
        return function;
    }

    @Override
    public SheetModel getSheet(Long uid) {
        for ( SheetModel sm : sheets ) {
            if ( uid != null && sm.getUID() == uid ) {
                return sm;
            }
        }
        log.log(Level.INFO, "Key {0} not found in project sheets list!", uid);
        return null;
    }

    @Override
    public void addSheet(SheetModel sheet) {
        if ( !sheets.contains(sheet)) {
            sheets.add(sheet);
        }
    }

    public Tile getCurrentSheetTile() {
        return currentSheetTile;
    }
    
    public void setCurrentSheetTile( long uid, Tile t ) {
        currentTileUID = uid;
        this.currentSheetTile = t;
        log.log(Level.INFO, 
                "Project current tile   tile:{0}",
                new Object[]{ t==null?"null":t.getMnemonic() }
        );
        notifyProjectChanged(this,ChangeType.TILE);
    }
    
    public Tile getFocusedMapTile() {
        return focusedBaseTile;
    }
    
    public Tile getFocusedFixtureTile() {
        return focusedItemTile;
    }

    public Tile getFocusedEntityTile() {
        return focusedEntityTile;
    }

    public void setFocusedMapTile( Tile  t ) {
        if ( focusedBaseTile != t ) {
            focusedBaseTile = t;
            notifyProjectChanged(this, ChangeType.FOCUS);
        }        
    }
    
    public void setFocusedFixtureTile( Tile t ) {
        if ( focusedItemTile != t ) {
            focusedItemTile = t;
            notifyProjectChanged(this, ChangeType.FOCUS);
        }
    }
    
    public void setFocusedEntityTile( Tile t ) {
        if ( focusedEntityTile != t ) {
            focusedEntityTile = t;
            notifyProjectChanged(this, ChangeType.FOCUS);
        }
    }
    
}
