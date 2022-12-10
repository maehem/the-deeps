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
 *
 * @author mark
 */
public abstract class TileProperty implements Cloneable {
    
    private Tile parent;
    private final String flagName;
    private String label = "";
    private String description = "";

    public TileProperty( Tile parent, String flagName ) {
        this.parent = parent;
        this.flagName = flagName;
        
    }
    
    public Tile getParent() {
        return parent;
    }
    
    protected void setParent( Tile parent ) {
        this.parent = parent;
    }
    
    public String getFlag() {
        return flagName;
    }
    
    /**
     * Return the human readable name/label if it was set by subclass.
     * Otherwise return the flag name, i.e. "BLOK"
     * 
     * @return pretty label/name for UI uses.
     */
    public String getLabel() {
        if ( label.isBlank() ) {
            return flagName;
        }
        
        return label;
    }
    
    public void setLabel( String text ) {
        this.label = text;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription( String desc ) {
        this.description = desc;
    }
    
    /**
     * Flag and data value suitable for file saving. i.e. BLOK99
     * @return flag and data value
     */
    public abstract String getFlagLump();
    
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        TileProperty t = (TileProperty) super.clone();
        log.log(Level.FINER, "Cloned TileProperty {0} -> {1}",
                new Object[]{this, t}
        );

        return t; 
    }
}
