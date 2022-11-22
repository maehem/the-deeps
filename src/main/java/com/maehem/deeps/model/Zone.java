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
package com.maehem.deeps.model;

import static com.maehem.deeps.Deeps.log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class Zone {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 30;
    
    private static final String BASE_PROP_KEY = "base.";
    private static final String ITEM_PROP_KEY = "item.";

    public static enum TileType { BASE, ITEM };

    private final ArrayList<ZoneListener> listeners = new ArrayList<>();
    
    private final GameModel gameModel;
    private final HashMap<Character, Long> sheetMap = new HashMap<>();
    
    private String name;
    private int width;
    private int height;
    private Tile[][] baseTile;
    private Tile[][] itemTile;

    public Zone( GameModel gm ) {
        this(gm, "Unnamed", WIDTH, HEIGHT);
    }

    public Zone(GameModel gm, String name, int width, int height) {
        this.gameModel = gm;
        this.name = name;
        this.width = width;
        this.height = height;
        this.baseTile = new Tile[height][width];
        this.itemTile = new Tile[height][width];

        initBaseTiles();
        initItemTiles();
    }

    private Zone( GameModel gm, String name, 
            HashMap<Character, Long> sheetMap, 
            ArrayList<String> base,
            ArrayList<String> item,
            Properties flags
    ) {
        this.gameModel = gm;
        this.name = name;
        this.sheetMap.putAll(sheetMap);
        
        // split line and count elements.  Set Width.
        
        // read lines until line is ,commentm, blank or whitespace.
        // Repeast for item tiles
        this.width = base.get(0).split(" ").length;
        this.height = base.size();
        baseTile = new Tile[height][width];
        itemTile = new Tile[height][width];
        
        for ( int y=0; y<height; y++ ) {
            String[] baseRow = base.get(y).split(" ");
            String[] itemRow = item.get(y).split(" ");
            for ( int x = 0; x<width; x++ ) {
                String baseCode = baseRow[x];
                String itemCode = itemRow[x];
                Long sheetUid = sheetMap.get(baseCode.charAt(0));
                SheetModel sheet = gm.getSheet(sheetUid);
                
                try {
                    // Clone the tile from the Sheet.
                    Tile sheetBaseTile = (Tile) sheet.getTile(
                            Integer.parseInt(baseCode.substring(1))
                    ).clone();
                    sheetBaseTile.setXY(x,y);
                    sheetBaseTile.setSheetCode(baseCode.charAt(0));
                    // Apply flags for this tile.
                    String pFlags = flags.getProperty(BASE_PROP_KEY + (y*width+x));
                    sheetBaseTile.applyFlags( pFlags );
                    this.baseTile[y][x] = sheetBaseTile;
                } catch (CloneNotSupportedException ex) {
                    log.log(Level.SEVERE, "Could not clone uid:" + sheetUid + ":" + baseCode, ex);
                }
                
                try {
                    // Clone the tile from the sheet.
                    Tile sheetItemTile = (Tile) sheet.getTile(
                            Integer.parseInt(itemCode.substring(1))
                    ).clone();
                    sheetItemTile.setXY(x,y);
                    sheetItemTile.setSheetCode(itemCode.charAt(0));
                    String pFlags = flags.getProperty(ITEM_PROP_KEY + (y*width+x));
                    sheetItemTile.applyFlags( pFlags );                    
                    this.itemTile[y][x] = sheetItemTile;
                } catch (CloneNotSupportedException ex) {
                    log.log(Level.SEVERE, "Could not clone uid:" + sheetUid + ":" + itemCode, ex);
                }
            }
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public Tile getTile(TileType t, int x, int y ) {
        switch (t) {
            case BASE:
                return baseTile[y][x];
            case ITEM:
                return itemTile[y][x];
            default:
                return null;
        }
    }
    
    public void setTile( TileType type, int x, int y, Tile t ) {
        switch( type ) {
            case BASE:
                t.setXY(x,y);
                baseTile[y][x] = t;
                break;
            case ITEM:
                t.setXY(x,y);
                itemTile[y][x] = t;
                break;
        }
    }

    /**
     * Swap existing tile for a newer one. 
     * Removes any listeners of old tile.
     * 
     * @param type
     * @param x
     * @param y
     * @param t the new tile.
     */
    public void swapTile(TileType type, int x, int y, Tile t) {
        Tile oldTile = null;
        switch( type ) {
            case BASE:
                oldTile = baseTile[y][x];
                oldTile.retire();
                t.setXY(x,y);
                baseTile[y][x] = t;
                break;
            case ITEM:
                oldTile = itemTile[y][x];
                t.setXY(x,y);
                itemTile[y][x] = t;
                break;
        }
        
        // Notify tile change.
        notifyTileSwapped(oldTile, t);        
    }

    private void initBaseTiles() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                baseTile[y][x] = new Tile("A00", x, y, null );
            }
        }
    }

    private void initItemTiles() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                itemTile[y][x] = new Tile("A00", x, y, null );
            }
        }
    }

    /**
     * load zone data
     * 
     * TODO: Read zone tile flag data, updating any defaults.
     * 
     * @param gm GameModel
     * @param in Input Stream
     * @return the loaded data as a Zone
     * @throws IOException
     * @throws ZoneFileFormatException 
     */
    public static Zone load(GameModel gm, InputStream in ) throws IOException, ZoneFileFormatException {
        HashMap<Character, Long> sheetMap = new HashMap<>();
        ArrayList<String> baseRows = new ArrayList<>();
        ArrayList<String> itemRows = new ArrayList<>();
        
        // Read each line
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        String line = br.readLine();

        // Lines with hash '#' are comments.
        // Look for line match "# Base Tiles"
        //line = br.readLine();
        while ( line != null && !line.startsWith("# name:")) {
            line = br.readLine();
        }
        if ( line == null ) {
            log.log(Level.SEVERE, "Zone file does not seem to have 'name:' defined!");
            throw new ZoneFileFormatException("Zone file does not seem to have 'name:' defined!");
        }
        String name = line.split(": ")[1];
        log.log(Level.INFO, "Reading Zone file for: " + name);

        while ( line != null && !line.startsWith("IDX")) {
            line = br.readLine();
        }
        if ( line == null ) {
            log.log(Level.SEVERE, "Zone file does not seem to have 'IDX' table defined!");
            throw new ZoneFileFormatException("Zone file does not seem to have 'IDX' table defined!");
        }
        
        while( line != null && line.startsWith("IDX")) {
            log.log(Level.INFO, "    Found IDX {0}", line.substring(4));
            String[] idxArray = line.split(":");  //   IDX:A:<big number>
            sheetMap.put(idxArray[1].charAt(0), Long.valueOf(idxArray[2]));
            line = br.readLine();
        }
       
        
        line = br.readLine();        
        while ( line != null && !line.startsWith("# Base Tiles") ) {
            line = br.readLine();
        }
        if ( line == null ) {
            throw new ZoneFileFormatException("Could not find '# Base Tiles' header!");
        }
        log.log( Level.FINER, "    found 'Base Tiles.'");
        
        line = br.readLine();  
        while ( line != null && line.matches("^[A-Z].*$") ) { // line must start with char A-Z
            log.log(Level.FINER, "        read Base tile line.");
            baseRows.add(line);
            line = br.readLine();
        }
        if ( baseRows.isEmpty() ) {
            throw new ZoneFileFormatException("'Base Rows' section did not parse! Check Zone file.");
        }
        
        while ( line != null && !line.startsWith("# Item Tiles") ) {
            line = br.readLine();
        }
        
        if ( line == null ) {
            throw new ZoneFileFormatException("Could not find '# Item Tiles' header!");
        }
        log.log(Level.FINER, "    found 'Item Tiles.'");
        line = br.readLine();
        while ( line != null && line.matches("^[A-Z].*$") ) { // line must start with char A-Z
            log.log(Level.FINER, "        read Item Tile line.");
            itemRows.add(line);
            line = br.readLine();
        }
        if ( itemRows.isEmpty() ) {
            throw new ZoneFileFormatException("'Item Rows' section did not parse! Check Zone file.");
        }
        
        if ( baseRows.size() != itemRows.size() ) {
            log.log(Level.WARNING, "    Base Rows count and Item rows count don't match!");
        }
        
        // Rest of file is properties.
        Properties p = new Properties();
        p.load(br);
        log.log(Level.INFO, "    Found {0} properties", p.size());
        
        
        return new Zone(gm, name, sheetMap, baseRows, itemRows, p);        
    }
    
    /**
     * Store Zone data.
     * 
     * TODO: Store tile flag data.
     * 
     * @param os
     * @return 
    */
    public boolean store(OutputStream os) {
        try ( BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            bw.write("# name: " + name);
            bw.newLine();
            bw.write("# Sheet map");
            bw.newLine();
            for ( Map.Entry<Character, Long> map :  sheetMap.entrySet() ) {
                bw.write("IDX:" + map.getKey() + ":" + map.getValue() );
                bw.newLine();
            }
            bw.newLine();
            bw.write("# Base Tiles");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bw.write(baseTile[y][x].getMnemonic());
                    bw.write(" ");
                }
                bw.newLine();
            }

            bw.newLine();
            bw.write("# Item Tiles");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bw.write(itemTile[y][x].getMnemonic());
                    bw.write(" ");
                }
                bw.newLine();
            }
            
            bw.newLine();
            
            // Write base tile properties
            bw.write("# Base Tile Flags");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bw.write(BASE_PROP_KEY + (y*width+x) + " = " + baseTile[y][x].getFlags());
                    bw.newLine();
                }
                bw.newLine(); // Gap after every row to make it easier to read.
            }
            
            // Write item tile properties
            bw.write("# Item Tile Flags");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bw.write(ITEM_PROP_KEY + (y*width+x) + " = " + itemTile[y][x].getFlags());
                    bw.newLine(); // Gap after every row to make it easier to read.
                }
                bw.newLine();
            }
            
            

        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    public SheetModel getSheet( Character key ) {
        log.log(Level.FINER, "Zone.getSheet( {0} )", key);
        log.log( Level.FINER, "Sheets:");
        for ( Entry<Character, Long> sm : sheetMap.entrySet() ) {
            log.log(Level.FINER, "    {0}:{1}", new Object[]{sm.getKey(), sm.getValue()});
        }
        return gameModel.getSheet(sheetMap.get(key));
    }
    
    public Character getKeyFor(long uid) {
        for ( Entry<Character, Long> hm : sheetMap.entrySet() ) {
            if ( hm.getValue().equals(uid)) {
                return hm.getKey();
            }
        }
        return null;
        
    }

    private void notifyTileChange( Tile t) {
        for ( ZoneListener l: listeners ) {
            l.zoneTileChanged(t);
        }
    }
    
    private void notifyTileSwapped( Tile tOld, Tile tNew) {
        for ( ZoneListener l: listeners ) {
            l.zoneTileSwapped(tOld, tNew);
        }
    }
    
    public void addListener(ZoneListener l) {
        listeners.add(l);
    }
    
    
}
