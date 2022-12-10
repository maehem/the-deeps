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
 * Tile that appears on Entity Layer.
 * Player, NPC, Enemy.
 * 
 * @author mark
 */
public class EntityTile extends Tile {

    public static final int NPC_DEFAULT        = -1;
    public static final int ENEMY_DEFAULT      = -1;
    public static final int ROLLING_DEFAULT    = -1;

    private int npc       = NPC_DEFAULT;            // -1 = not a NPC.  0-999 = npc index from game.
    private int enemy     = ENEMY_DEFAULT;          // -1 = not enemy. 0-999 = enemy from game.     
    private int rolling   = ROLLING_DEFAULT;        // -1 = rail thing, moves on rails.

    
    public EntityTile(Zone zone, Character sheet, int index, int x, int y, String props) {
        super(zone, sheet, index, x, y );//, props);
        applyFlags(props);
    }

    public EntityTile(Zone zone, int index, int x, int y, String props) {
        super(zone, index, x, y );//, props);
        applyFlags(props);
    }
    
    /**
     * @return the npc
     */
    public int getNpc() {
        return npc;
    }

    /**
     * @param npc the npc to set
     */
    public void setNpc(int npc) {
        this.npc = npc;
    }

    /**
     * @return the enemy
     */
    public int getEnemy() {
        return enemy;
    }

    /**
     * @param enemy the enemy to set
     */
    public void setEnemy(int enemy) {
        this.enemy = enemy;
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

    
    @Override
    protected void configureFlagSetting( String flag  ) {
        
        Character f = flag.charAt(0);
        // Addtional flag considerations.
        switch( f ) {
            case 'C': // Character Player, NPC  CHAR999 : C<idNumber>
                log.log(Level.FINER, "   Character.");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setNpc(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
            case 'E': // Enemy, NPC  ENMY999 : E<idNumber>
                log.log(Level.FINER, "   Enemy:");
                if (flag.length() > 1) {
                    String num = flag.substring(1);
                    setEnemy(Integer.parseInt(num));
                    log.log(Level.FINER, "    n = {0}", num);
                }
                break;
        }        
    }
    
    @Override
    public String getFlags() {
        super.getFlags();
        
        StringBuilder sb = new StringBuilder();
        
        if ( getEnemy() != ENEMY_DEFAULT ) {
            sb.append("E").append(getEnemy()).append(":");
        }
        if ( getNpc()!= NPC_DEFAULT ) {
            sb.append("C").append(getNpc()).append(":");
        }
        if ( getRolling() != ROLLING_DEFAULT ) {
            sb.append("R").append(getRolling()).append(":");
        }

        sb.append("D").append(getDescription());
        
        return sb.toString();
    }
    
    
}
