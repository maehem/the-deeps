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
import java.util.logging.Level;

/**
 * Tile that appears on Fixture Layer.
 * Things that decorate a map, items that can be picked up.
 * 
 * Might block or slow player.
 * 
 * @author mark
 */
public class FixtureTile extends Tile {

    public static final int TRACK_DEFAULT      = -1;
    public static final int UMBRA_DEFAULT      = -1;
    public static final int WEAPON_DEFAULT     = -1;
    public static final int ABLATION_DEFAULT   = -1; // -1 = can't wear, 0-99 wears down over time.
    public static final int ROLLING_DEFAULT    = -1;
    public static final int INVENTORY_DEFAULT  = -1;
    public static final int STORAGE_DEFAULT    = -1;
    
    public static final int UMBRA_MAX = 99;

    private int slowing = 0; // 0-99. Slows entity by % when walking here.
    private int harvestable = -1; // HP to harvest. -1=no. 0=free to pick. 1-99 Nailed down, hits needed.
    private boolean autoHarvest = false; // Will go into player inventory if free.
    
    private int ablation  = ABLATION_DEFAULT;       // -1 = cannont be worn/damaged. >= 0.  degrades over time. Max 99
    private int inventoryItem = INVENTORY_DEFAULT;  // -1 = not inventory. > 000-999 index in game
    private int rolling   = ROLLING_DEFAULT;        // -1 = rail thing, moves on rails.
    private int storage   = STORAGE_DEFAULT;        // -1 = cannot store items. 0-999 = game index of storage item.
    private int track     = TRACK_DEFAULT;          // -1 = not track, track element, can move 'rolling' items.
    private int umbra     = UMBRA_DEFAULT;          // -1 = not casting shade, 0-99 cast drop shadow.
    private int weapon    = WEAPON_DEFAULT;         // -1 = not weapoon. 000-999 = damage. 0 = damaged weapon

    public FixtureTile(Zone zone, Character sheet, int index, int x, int y, String props) {
        super(zone, sheet, index, x, y, props);
        
        // Apply props (local)
        applyFlags(props);
    }

    public FixtureTile(Zone zone, int index, int x, int y, String props) {
        super(zone, index, x, y, props);
        
        // Apply props (local)
        applyFlags(props);
    }
    
    public int getAblation() {
        return ablation;
    }
    
    public void setAblation( int newValue )  {
        ablation = newValue;
    }
    
    public void applyAblation(int amount) {
        if (canAblate()) {
            setAblation(getAblation() + amount);
        }
        if (getAblation() < 0) {
            setAblation(0);
        }
        if (getAblation() > 99) {
            setAblation(99);
        }
    }
    
    public boolean canAblate() {
        return getAblation() >= 0;
    }

    public int getInventoryItem() {
        return inventoryItem;
    }
    
    public void setInventoryItem( int i) {
        this.inventoryItem = i;
    }
    
    public boolean isInventoryItem() {
        return inventoryItem >= 0;
    }
    
    /**
     * @return the rolling
     */
    public boolean isRolling() {
        return rolling>=0;
    }

    /**
     * Get value of 'rolling', game's version of direction and speed.
     * 
     * @return 
     */
    public int getRolling() {
        return rolling;
    }
    
    /**
     * @param rolling the rolling to set
     */
    public void setRolling(int rolling) {
        this.rolling = rolling;
    }

    /**
     * @return the storage
     */
    public int getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getUmbra() {
        return umbra;
    }
    
    public void setUmbra( int umbra ) {
        this.umbra = umbra;
        notifyPropertyChanged("umbra");
    }
    
    public int getWeapon() {
        return weapon;
    }
    
    public void setWeapon(int damage) {
        this.weapon = damage;
    }
    
    public boolean isWeapon() {
        return weapon >= 0;
    }
    
    /**
     * @return the track
     */
    public boolean isTrack() {
        return track>=0;
    }

    public int getTrack() {
        return track;
    }
    
    /**
     * @param track the track to set
     */
    public void setTrack(int track) {
        this.track = track;
    }
    
    @Override
    public void configureFlagSetting( String flag ) {
        log.log(Level.FINE, "Configure Fixture Tile Setting: {0}  {1}", 
                new Object[]{getMnemonic(), flag });
        Character f = flag.charAt(0);
        // Addtional flag considerations.
        switch( f ) {
            case 'A': // Ablation, WEAR  WEAR999 : E<idNumber>
                log.log(Level.FINER, "   Ablation:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setAblation(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
             case 'I': // Inventory Item  INVT999 : I<idNumber>
                log.log(Level.FINER, "   Item:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setInventoryItem(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
            case 'S': // Storage(chest), STOR  STOR999 : S<idNumber>
                log.log(Level.FINER, "   Storage:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setStorage(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
            case 'T': // Track, TRAK  TRAK99 : T<idNumber>
                log.log(Level.FINER, "   Track:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setTrack(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
            case 'W': // Weapon, WEAP  WEAP999 : W<idNumber>
                log.log(Level.FINER, "   Storage:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setWeapon(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
            case 'U': // Cast shadow,  SHAD99 :  U<int>
                log.log(Level.FINER, "   Umbra:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setUmbra(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                } else {
                    setUmbra(UMBRA_MAX);
                    log.log(Level.FINER, "    n = umbra max");
                }
                break;
        }        
    }

    @Override
    public String getFlags() {
        StringBuilder sb = new StringBuilder(super.getFlags());
        sb.append(":");
        
        if ( getAblation() != ABLATION_DEFAULT ) {
            sb.append("A").append(getAblation()).append(":");
        }
        if ( getInventoryItem() != INVENTORY_DEFAULT ) {
            sb.append("I").append(getInventoryItem()).append(":");
        }
        if ( getRolling() != ROLLING_DEFAULT ) {
            sb.append("R").append(getRolling()).append(":");
        }
        if ( getStorage() != STORAGE_DEFAULT ) {
            sb.append("S").append(getStorage()).append(":");
        }
        if ( getTrack() != TRACK_DEFAULT ) {
            sb.append("T").append(getTrack()).append(":");
        }
        if ( getUmbra() != UMBRA_DEFAULT ) {
            sb.append("U").append(getUmbra()).append(":");
        }
        if ( getWeapon() != WEAPON_DEFAULT ) {
            sb.append("W").append(getWeapon()).append(":");
        }
        if ( sb.lastIndexOf(":") == sb.length()-1 ) {
            //log.log(Level.INFO, "Trim colon from end of flags: {0}", sb.toString());
            //log.log(Level.INFO, "    ===> {0}", sb.substring(0, sb.length()-1));
            return sb.substring(0, sb.length()-1);
        }
        return sb.toString();
    }
}
