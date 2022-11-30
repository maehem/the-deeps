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
 *  TODO: Implement 4-char Flags
 * 
 * @author Mark J Koch ( GitHub @maehem)
 */
public abstract class Tile implements Cloneable {

    private ArrayList<TileListener> listeners = new ArrayList<>();
    
    private Zone zone;
    private Character sheet;
    private final int index;
    private int x; //  Grid X
    private int y; //   Grid Y
    
    private String description = ""; // String description
    
    public static final int BLOCKING_DEFAULT   = -1;
    public static final int BLOCKING_MAX = 99;

    public static final int LUMINOUS_DEFAULT   = -1;
    public static final int SOUND_DEFAULT      = -1;

    private int blocking  = BLOCKING_DEFAULT;       // -1 = not blocking,  0-99  blocks/slows by this amount (99 is default)
    private int luminous  = LUMINOUS_DEFAULT;       // -1 = no light emmited.  00-99, light effect # in game engine.
    private int sound     = SOUND_DEFAULT;          // -1 = no sound emitted. 0-999 = sound index from a game table.

    
    public Tile(Zone zone, Character sheet, int index, int x, int y, String props) {
        this.zone = zone;
        this.sheet = sheet;
        this.index = index;
        //this.mnemonic = mnemonic;
        this.x = x;
        this.y = y;
                   
        applyFlags(props);  // colon :  separated list. 
    }
    
    /**
     * Blank Tile.
     * 
     * @param zone
     * @param index
     * @param x
     * @param y
     * @param props 
     */
    public Tile(Zone zone, int index, int x, int y, String props) {
        this( zone, '_', index, x, y, props);
    }
        
    protected final void applyFlags(String props) {
        if (props != null && props.length() > 0) {
            String[] flags = props.split(":");
            log.log(Level.FINER,
                    "Tile {0} has {1} items.",
                    new Object[]{getMnemonic(), flags.length}
            );

            for (String flag : flags) {
                log.log(Level.FINER, "    flag: {0}", flag);
                //toggleMapFlag(flag.charAt(0));
                configureFlagSetting(flag);
            }
        } else {
            log.log(Level.INFO, "No props for {0}", getMnemonic());
        }
    }

    protected void configureFlagSetting( String flag  ) {
        Character f = flag.charAt(0);
        // Addtional flag considerations.
        switch( f ) {
            case 'B': // Blocking  0-99 (99=default, can be blank)
                if ( flag.length()>1) {
                    setBlocking(Integer.parseInt(flag.substring(1)) );
                } else {
                    setBlocking( BLOCKING_MAX );
                }
                break;
            case 'D': // Description    DESC<string>  : D<string>
                if ( flag.length() > 1 ) {
                    this.description = flag.substring(1);
                }
                break;
            case 'F': // Foley/Sound, SNDX  SNDX999 : F<idNumber>
                log.log(Level.FINER, "   SoundFX:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setSound(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
           case 'L': // LuminousFX  LUMI999 : L<idNumber>
                log.log(Level.FINER, "   Luminous:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setLuminous(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
        }        
    }
    
    public String getMnemonic() {
        return sheet + String.format("%03d", index);
    }

    public Zone getZone() {
        return zone;
    }
    
    public void setZone(Zone zone) {
        this.zone = zone;
    }
    
    public Character getSheet() {
        return sheet;
    }
    
    public boolean setSheet( Character sheet ) {
        if ( sheet.equals(getSheet() )) {
            return true;
        }
        if (sheet < 65 || sheet > 90) { // A-Z character
            log.log(Level.SEVERE, "Tile.setSheet(): Sheet code {0} is out of range in Tile!", sheet.toString());
            //Thread.dumpStack();
            return false;
        }
        String zoneName = "NONE";
        if ( getZone() != null ) {
            zoneName = getZone().getName();
        }
        log.log(Level.FINE, "Tile at {0}x{1} in zone: {2} sheet changed from {3} -> {4} obj: {5}",
                new Object[]{ getX(), getY(), zoneName, getSheet(), sheet, this.toString() }
        );
        this.sheet = sheet;
        notifyMnemonicChanged();
        return true;
    }
    
    public int getIndex() {
        return index;
    }
    
    /**
     * Grid X location in map.
     * 
     * @return grid X location
     */
    public int getX() {
        return x;
    }
    
    /**
     * Set the X grid location in map
     * 
     * @param x location
     */
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
    
    /**
     * Set the Y grid location in map
     * 
     * @param y location
     */
    public void setY( int y ) {
        this.y = y;
    }
    
    /**
     * Set the grid location in map
     * 
     * @param x
     * @param y 
     */
    public void setXY( int x, int y ) {
        this.x = x;
        this.y = y;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription( String s) {
         this.description = s;
     }
     
    public final void addListener(TileListener l) {
        log.log(Level.FINE, "Tile: Add Tile Listener: {0}x{1}  obj:{2}", 
                new Object[]{ getX(), getY(), l.toString() });
        listeners.add(l);
    }

    public final Object getListeners() {
        return listeners.toArray( );
    }
    
    public final boolean removeListener(TileListener l) {
        return listeners.remove(l);
    }
    
    /**
     * Make sure we release resources when not being used.
     * 
     */
    public void retire() {
        listeners.clear();
    }
    
    private void notifyMnemonicChanged() {
        log.log(Level.FINE, "Tile.notifyMnemonicChanged: {0}x{1}  {2}",
                new Object[]{ getX(), getY(), getMnemonic() });
        for (TileListener l : listeners) {
            log.log(Level.FINER, "Tile.mnemonicListener: {0}", l.toString());
            l.tileCodeChanged(this);
        }
    }

    public int getLuminous() {
        return luminous;
    }
    
    public void setLuminous( int lum ) {
        this.luminous = lum;
    }
    
    public int getBlocking() {
        return blocking;
    }
    
    public void setBlocking( int val) {
        this.blocking = val;
    }
    
    public boolean isBlocking() {
        return blocking>=0;
    }
    
    /**
     * @return the sound
     */
    public int getSound() {
        return sound;
    }

    /**
     * @param soundFx the game engine soundFx # to set
     */
    public void setSound(int soundFx) {
        this.sound = soundFx;
    }

    public String getFlags() {
        //  I, W, T, R, C, E, M, N, S, U
        StringBuilder sb = new StringBuilder();
        
        if ( getBlocking() != BLOCKING_DEFAULT ) {
            if ( getBlocking() < 99 ) {
                sb.append("B").append(getBlocking()).append(":");
            } else {
                sb.append("B").append(":");                
            }
        }
        if ( getLuminous()!= LUMINOUS_DEFAULT ) {
            sb.append("L").append(getLuminous()).append(":");
        }
        if ( getSound() != SOUND_DEFAULT ) {
            sb.append("F").append(getSound()).append(":");
        }

        sb.append("D").append(getDescription());
        
        return sb.toString();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Tile t = (Tile) super.clone();
        log.log(Level.FINER, "Cloned tile {0} -> {1}",
                new Object[]{this, t}
        );
        t.listeners = new ArrayList<>();
        t.zone = null;
        t.sheet = '_';
        return t; 
    }
     
}
