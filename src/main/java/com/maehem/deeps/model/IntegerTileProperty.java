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
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

/**
 *
 * @author mark
 */
public class IntegerTileProperty extends TileProperty {
    public enum EditStyle { TEXT_FIELD, SLIDER, SPINNER }
    
    public final static int MIN_VAL = -1;
    public final static int MAX_VAL = 99;
    public final static int DEFAULT_VAL = MIN_VAL;
    
    private int value = DEFAULT_VAL;
    private int minValue = MIN_VAL;
    private int maxValue = MAX_VAL;
    private EditStyle editStyle = EditStyle.TEXT_FIELD;

    public IntegerTileProperty( Tile parent, String flag ) {
        super(parent, flag);
    }
    
    public IntegerTileProperty( Tile parent, String flag, String label, int min, int max, int val, EditStyle style ) {
        super(parent, flag);
        
        this.setLabel(label);
        
        this.minValue = min;
        this.maxValue = max;
        this.value = val;
        this.editStyle = style;
    }
    
    public int getMinValue() {
        return minValue;
    }
    
    public int getMaxValue() {
        return maxValue;
    }
    
    public int getValue() {
        return value;
    }
    
    public EditStyle getEditStyle() {
        return editStyle;
    }
    
    public void setEditStyle( EditStyle style ) {
        this.editStyle = style;
    }
    
    /**
     * Set value with range clamping.
     * 
     * @param val 
     */
    public void setValue(int val) {
        if ( val == value ) { return; }
        if ( val < minValue ) {
            value = minValue;
        } else  if ( val > maxValue ) {
            value = maxValue;
        } else {
            log.log(FINER, "IntegerTileProperty value change:  old={0} new={1}", 
                    new Object[]{value, val});
            value = val;
        }
        
        // todo: notify value changed.
        getParent().notifyPropertyChanged(this);
    }

    @Override
    public String getFlagLump() {
        if ( value != DEFAULT_VAL ) {
            return getLabel() + value;
        } else {
            return "";
        }
    }
}
