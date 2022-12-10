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
    private static final String ENTITY_PROP_KEY = "entity.";

    //public static enum TileType { BASE, ITEM };

    private final ArrayList<ZoneListener> listeners = new ArrayList<>();
    
    private final GameModel gameModel;
    private final HashMap<Character, Long> sheetMap = new HashMap<>();
    
    private String name;
    private int width;
    private int height;
    private MapTile[][] baseTile;
    
    private ArrayList<FixtureTile> fixtures = new ArrayList<>();
    private ArrayList<EntityTile> entities = new ArrayList<>();

    /**
     * Construct a new Zone using data, usually from a dialog.
     * 
     * @param gm GameModel, usually the Zone editor.
     * @param name of this Zone.
     * @param width in cells.
     * @param height in cells.
     */
    public Zone(GameModel gm, String name, int width, int height) {
        this.gameModel = gm;
        this.name = name;
        this.width = width;
        this.height = height;
        this.baseTile = new MapTile[height][width];

        // Full map with default tiles.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                baseTile[y][x] = new MapTile(this, 0, x, y, null );
            }
        }
    }

    /**
     * Construct a zone from data read from a file.
     * 
     * @param gm GameModel, usually the game engine.
     * @param name of the Zone
     * @param sheetMap map of character index to UID for each sheet.
     * @param base a row of mnemonics for the map.
     * @param flags properties for map and other item tiles.
     */
    private Zone( GameModel gm, String name, 
            HashMap<Character, Long> sheetMap, 
            ArrayList<String> base,
            //ArrayList<String> item,
            Properties flags
    ) {
        this.gameModel = gm;
        this.name = name;
        this.sheetMap.putAll(sheetMap);
        
        // split line and count elements.  Set Width.
        this.width = base.get(0).split(" ").length;
        this.height = base.size();
        baseTile = new MapTile[height][width];
        
        for ( int y=0; y<height; y++ ) {
            String[] baseRow = base.get(y).split(" ");
            for ( int x = 0; x<width; x++ ) {
                String baseCode = baseRow[x];
                Long sheetUid = sheetMap.get(baseCode.charAt(0));
                SheetModel sheet = gm.getSheet(sheetUid);
                
                try {
                    // Clone the tile from the Sheet.
                    Tile sheetBaseTile = (Tile) sheet.getTile(
                            Integer.parseInt(baseCode.substring(1))
                    ).clone();
                    if ( sheetBaseTile instanceof MapTile ) {
                        sheetBaseTile.setZone(this);
                        sheetBaseTile.setXY(x,y);
                        sheetBaseTile.setSheet(baseCode.charAt(0));
                        // Apply flags for this tile.
                        String pFlags = flags.getProperty(BASE_PROP_KEY + (y*width+x));
                        sheetBaseTile.applyFlags( pFlags );
                        this.baseTile[y][x] = (MapTile) sheetBaseTile;
                    } else {
                        log.log(Level.SEVERE, 
                                "Zone constructor: Tile is NOT MapTile! class:{0}", 
                                sheetBaseTile.getClass().getSimpleName()
                        );
                    }
                } catch (CloneNotSupportedException ex) {
                    log.log(Level.SEVERE, "Could not clone uid:" + sheetUid + ":" + baseCode, ex);
                }
                
                // Process any FixtureTile at this grid location
                String pFlags = flags.getProperty(ITEM_PROP_KEY + (y * width + x));
                if (pFlags != null) {
                    String mnemonic = pFlags.split(":")[0];
                    try {
                        // Clone the tile from the sheet.
                        Tile sheetItemTile = (Tile) sheet.getTile(
                                Integer.parseInt(mnemonic.substring(1))
                        ).clone();
                        if (sheetItemTile instanceof FixtureTile) {
                            sheetItemTile.setZone(this);
                            sheetItemTile.setXY(x, y);
                            sheetItemTile.setSheet(mnemonic.charAt(0));
                            sheetItemTile.applyFlags(pFlags.substring(pFlags.indexOf(":") + 1));
                            fixtures.add((FixtureTile) sheetItemTile);
                        } 
//                        if (sheetItemTile instanceof EntityTile) {
//                            sheetItemTile.setZone(this);
//                            sheetItemTile.setXY(x, y);
//                            sheetItemTile.setSheet(mnemonic.charAt(0));
//                            sheetItemTile.applyFlags(pFlags.substring(pFlags.indexOf(":") + 1));
//                            entities.add((EntityTile) sheetItemTile);
//                        }
                        else {
                            log.log(Level.SEVERE, 
                                    "Item tile at {0},{1} is not a Fixture Tile! Obj:{2}",
                                    new Object[]{x, y, sheetItemTile});
                        }
                    } catch (CloneNotSupportedException ex) {
                        log.log(Level.SEVERE, "Could not clone uid:" + sheetUid + ":" + mnemonic, ex);
                    }
                }
                // Process any FixtureTile at this grid location
                pFlags = flags.getProperty(ENTITY_PROP_KEY + (y * width + x));
                if (pFlags != null) {
                    String mnemonic = pFlags.split(":")[0];
                    try {
                        // Clone the tile from the sheet.
                        Tile sheetItemTile = (Tile) sheet.getTile(
                                Integer.parseInt(mnemonic.substring(1))
                        ).clone();
//                        if (sheetItemTile instanceof FixtureTile) {
//                            sheetItemTile.setZone(this);
//                            sheetItemTile.setXY(x, y);
//                            sheetItemTile.setSheet(mnemonic.charAt(0));
//                            sheetItemTile.applyFlags(pFlags.substring(pFlags.indexOf(":") + 1));
//                            fixtures.add((FixtureTile) sheetItemTile);
//                        }
                        if (sheetItemTile instanceof EntityTile) {
                            sheetItemTile.setZone(this);
                            sheetItemTile.setXY(x, y);
                            sheetItemTile.setSheet(mnemonic.charAt(0));
                            sheetItemTile.applyFlags(pFlags.substring(pFlags.indexOf(":") + 1));
                            entities.add((EntityTile) sheetItemTile);
                        }else {
                            log.log(Level.SEVERE, 
                                    "Item tile at {0},{1} is not a Entity Tile! Obj:{2}",
                                    new Object[]{x, y, sheetItemTile});
                        }
                    } catch (CloneNotSupportedException ex) {
                        log.log(Level.SEVERE, "Could not clone uid:" + sheetUid + ":" + mnemonic, ex);
                    }
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

    public boolean removeFixture( int x, int y ) {
        FixtureTile t = getFixtureTile(x, y);
        boolean result = false;
        if ( t != null ) {
            result = fixtures.remove(t);
            log.log(Level.INFO, 
                    "Removed existing FixtureTile at: {0}x{1}", 
                    new Object[]{t.getX(), t.getY()}
            );
            notifyTileSwapped(t, null); 
        }
        
        return result;
    }
    
    public boolean removeEntity( int x, int y ) {
        EntityTile t = getEntityTile(x, y);
        boolean result = false;
        if ( t != null ) {
            result = entities.remove(t);
            log.log(Level.INFO, 
                    "Removed existing EntityTile at: {0}x{1}", 
                    new Object[]{t.getX(), t.getY()}
            );
            notifyTileSwapped(t, null); 
        }
        
        return result;
    }
    
    /**
     * Swap existing tile for a newer one. 
     * Removes any listeners of old tile.
     * 
     * @param x
     * @param y
     * @param t the new tile.
     */
    public void swapTile(/*TileType type,*/ int x, int y, Tile t ) {
        Tile oldTile;
        if ( t instanceof MapTile ) {
            log.log(Level.INFO, "Swapping a MapTile.");
            oldTile = baseTile[y][x];
            oldTile.retire();
            t.setXY(x,y);
            baseTile[y][x] = (MapTile) t;
            // Notify tile change.
            notifyTileSwapped(oldTile, t);        
        } else if ( t instanceof FixtureTile ) {
            log.log(Level.INFO, "Swapping a FixtureTile.");
            t.setXY(x,y);
            oldTile = getFixtureTile(x, y);
            if ( oldTile != null ) {
                fixtures.remove((FixtureTile)oldTile);
                log.log(Level.INFO, 
                        "Removed existing FixtureTile at: {0}{1}", 
                        new Object[]{oldTile.getX(), oldTile.getY()}
                );
            }
            fixtures.add((FixtureTile)t);
            // Notify tile change.
            notifyTileSwapped(oldTile, t); 
        } else if ( t instanceof EntityTile ) {
            log.log(Level.INFO, "Swapping a EntityTile.");
            t.setXY(x,y);
            oldTile = getEntityTile(x, y);
            if ( oldTile != null ) {
                entities.remove((EntityTile)oldTile);
                log.log(Level.INFO, 
                        "Removed existing EntityTile at: {0}{1}", 
                        new Object[]{oldTile.getX(), oldTile.getY()}
                );
            }
            entities.add((EntityTile)t);
            // Notify tile change.
            notifyTileSwapped(oldTile, t); 
        } else {
            log.log( Level.WARNING, "Swapping a unknown type tile.");
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
        //ArrayList<String> itemRows = new ArrayList<>();
        
        // Read each line
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        String line = br.readLine();

        // Lines with hash '#' are comments.
        // Look for line match "# Base Tiles"
        while ( line != null && !line.startsWith("# name:")) {
            line = br.readLine();
        }
        if ( line == null ) {
            log.log(Level.SEVERE, "Zone file does not seem to have 'name:' defined!");
            throw new ZoneFileFormatException("Zone file does not seem to have 'name:' defined!");
        }
        String name = line.split(": ")[1];
        log.log(Level.INFO, "Reading Zone file for: {0}", name);

        while ( line != null && !line.startsWith("IDX")) {
            line = br.readLine();
        }
        if ( line == null ) {
            log.log(Level.SEVERE, "Zone file does not seem to have 'IDX' table defined!");
            throw new ZoneFileFormatException("Zone file does not seem to have 'IDX' table defined!");
        }
        
        while( line != null && line.startsWith("IDX")) {
            log.log(Level.CONFIG, "    Found IDX {0}", line.substring(4));
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
        
        // Rest of file is properties.
        Properties p = new Properties();
        p.load(br);
        log.log(Level.FINE, "    Found {0} properties", p.size());
        
        return new Zone(gm, name, sheetMap, baseRows, p);        
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
            
            // Write base tile properties
            bw.write("# Base Tile Flags");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bw.write(BASE_PROP_KEY + (y*width+x) + " = " + baseTile[y][x].getFlags());
                    bw.newLine();
                }
                bw.newLine();
            }
            
            // Write fixture tile properties
            bw.write("# Fixture Tile Flags");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    FixtureTile t = getFixtureTile(x, y);
                    if ( t != null ) {
                        bw.write(ITEM_PROP_KEY + (y*width+x) + " = " + 
                                t.getMnemonic() + ":" +
                                t.getFlags()
                        );     
                        bw.newLine();
                    }
                }
            }
            bw.newLine();

            // Write entity tile properties
            bw.write("# Entity Tile Flags");
            bw.newLine();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    EntityTile t = getEntityTile(x, y);
                    if ( t != null ) {
                        bw.write(ENTITY_PROP_KEY + (y*width+x) + " = " + 
                                t.getMnemonic() + ":" +
                                t.getFlags()
                        );
                        bw.newLine();
                    }
                }
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
    
    public FixtureTile getFixtureTile( int x, int y ) {
        for( FixtureTile t: fixtures ) {
            if ( t.getX() == x && t.getY() == y ) {
                return t;
            }
        }
        
        return null;
    }
    
    public EntityTile getEntityTile( int x, int y ) {
        for( EntityTile t: entities ) {
            if ( t.getX() == x && t.getY() == y ) {
                return t;
            }
        }
        
        return null;
    }
    
    public MapTile getMapTile( int x, int y ) {
        return baseTile[y][x];
    }
    
}
