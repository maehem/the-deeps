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
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class Tile implements Cloneable {

    private final ArrayList<TileListener> listeners = new ArrayList<>();
    
    private boolean map = true;  // true if base map tile.
    private String mnemonic; //  <Char><(int)nnn>  A00, C34, etc.
    private int x; //  LayoutX
    private int y; //  LayoutY
    
    private String description = "";
    private int inventoryItem = -1; // -1 = not inventory. > 000-999 index in game
    private int weapon = -1;        // -1 = not weapoon. 000-999 = damage. 0 = damaged weapon
    private int wear = 99;          // -1 = cannont be worn/damaged. >= 0.  degrades over time. Max 99

    public Tile( String mnemonic, String props ) {
        this(mnemonic, 0, 0, props);
    }

    public Tile(String mnemonic, int x, int y, String props) {
        this.mnemonic = mnemonic;
        this.x = x;
        this.y = y;
            
        if (props != null) {
            // Setup flags from  sm tile properties.
            // colon :  separated list.
            //String props = sm.getPropsFor(getTileNum());
            if (props.length() > 0) {
                String[] flags = props.split(":");
                log.log(Level.FINER,
                        "Tile {0} has {1} items.",
                        new Object[]{mnemonic, flags.length}
                );

                for (String flag : flags) {
                    log.log(Level.FINER, "    flag: {0}", flag);
                    toggleMapFlag(flag.charAt(0));                    
                    configureFlagSettings(flag);
                }
            } else {
                log.log(Level.INFO, "No props for {0}", mnemonic);
            }
        }
    }

    private void toggleMapFlag( Character f ) {
        //    Non-Map:  I, W, T, R, C, E, M, N 
        switch ( f ) {
            case 'I':  // INVT . inventory item (can be picked up)
            case 'W':  // WEAP . weapon
            case 'T':  // TRAK . track
            case 'R':  // ROLL . rail car
            case 'C':  // CHAR . character
            case 'E':  // ENMY . enemy
            case 'N':  // NMAP . other non-map
            case 'S':  // STOR . storage
            case 'M':  // MOUS . mouse cursor
            case 'U':  // SHAD . cast shadow
                this.map = false;
                break;
        }
    }
    
    private void configureFlagSettings( String flag  ) {
        Character f = flag.charAt(0);
        // Addtional flag considerations.
        switch( f ) {
            case 'D': // Description    DESC<string>  : D<string>
                if ( flag.length() > 1 ) {
                    this.description = flag.substring(1);
                }
                break;
            case 'I': // Inventory Item  INVT999 : I<idNumber>
                log.log(Level.FINER, "   Item.");
                String num = flag.substring(1);
                if (num.length() > 0) {
                    log.log(Level.FINER, "    n = {0}", Integer.valueOf(num));
                }
                break;
            case 'C': // Character Player, NPC  CHAR999 : C<idNumber>
                log.log(Level.FINER, "   Character.");
                break;
            case 'E': // Enemy, NPC  ENMY999 : E<idNumber>
                log.log(Level.FINER, "   Enemy.");
                break;
            case 'S': // Storage(chest), NPC  STOR999 : S<idNumber>
                log.log(Level.FINER, "   Storage.");
                break;
            case 'U': // Cast shadow,  SHAD :  U  [boolean]
        }        
    }
    
    public String getMnemonic() {
        return mnemonic.charAt(0) + 
                String.format("%03d", Integer.valueOf(mnemonic.substring(1)));
    }

    public boolean setMnemonic(String code) {
        if (code.charAt(0) < 65 || code.charAt(0) > 90) { // A-Z character
            log.log(Level.SEVERE, "Tile.setCode(): Sheet code {0} is out of range in Tile!", code);
            //Thread.dumpStack();
            return false;
        }
        // Pad the code such that A0 would result in A000
        this.mnemonic = code.charAt(0) + 
                String.format("%03d", Integer.valueOf(code.substring(1)));
        log.log( Level.INFO, "Monemonic padded to: " + this.mnemonic );
        notifyMnemonicChanged();
        return true;
    }

    public Character getSheetReference() {
        return mnemonic.charAt(0);
    }

    public final int getTileNum() {
        return Integer.parseInt(mnemonic.substring(1));
    }

    /**
     * Grid X location in map.
     * 
     * @return grid X location
     */
    public int getX() {
        return x;
    }
    
    public void setX( int x ) {
        this.x = x;
    }
    
    /**
     * Grid Y location in map.
     * 
     * @return grid Y location
     */
    public int getY() {
        return y;
    }
    
    public void setY( int y ) {
        this.y = y;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription( String s) {
         this.description = s;
     }
     
    public final void addListener(TileListener l) {
        listeners.add(l);
    }

    private void notifyMnemonicChanged() {
        for (TileListener l : listeners) {
            l.tileCodeChanged(this);
        }
    }

    public boolean isMapTile() {
        return map;
    }

    public void setMap(boolean map) {
        this.map = map;
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
    
    public int getWeapon() {
        return weapon;
    }
    
    public void setWeapon(int damage) {
        this.weapon = damage;
    }
    
    public boolean isWeapon() {
        return weapon >= 0;
    }
    
    public int getWear() {
        return wear;
    }
    
    public void setWear( int newValue )  {
        wear = newValue;
    }
    
    public void applyWear( int amount ) {
        if ( canWear() )
            setWear(getWear() + amount);
            if (getWear() < 0 ) {
                setWear(0);
            }
            if ( getWear() > 99 ) {
                setWear(99);
            }
    }
    
    public boolean canWear() {
        return getWear() >= 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); 
    }

    public void setSheetCode(char c) {
        this.mnemonic = String.valueOf(c) + mnemonic.substring(1);
        notifyMnemonicChanged();
    }

    /**
     * Make sure we release resources when not being used.
     * 
     */
    public void retire() {
        listeners.clear();
    }
    
    
}
