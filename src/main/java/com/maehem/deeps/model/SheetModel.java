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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import javafx.scene.image.Image;

/**
 *
 * @author Mark J Koch ( GitHub @maehem)
 */
public class SheetModel {

    public enum NonMapKeys {
        I, W, T, R, C, E
    }

    private long uid;
    private int size;
    private int width;
    private int height;
    private String name;
    //private Character index;
    private String author;
    private final String path;
    private final Properties properties = new Properties();

    private Image tileMap;
    private final double fidelity = 4.0;
    
    private ArrayList<Tile> tiles = new ArrayList<>();
    //private Tile tiles[];

    /**
     * Create sheet model using a resource that's in a JAR file. Used in a
     * packaged game.
     *
     * @param path within a jar file
     * @throws IOException
     */
    public SheetModel(String path) throws IOException {
        // TODO: Check if tileMap is NULL and do something about it.
        this.path = path;
        InputStream props = getClass().getResourceAsStream(
                getPropertiesFileName()
        );
        InputStream imageStream = getClass().getResourceAsStream(path);
        InputStream imageStreamHiFi = getClass().getResourceAsStream(path);

        initProps(props, imageStream, imageStreamHiFi);
    }

    /**
     * Create a sheet model using a file that's within our local disk. Used for
     * the editor utility.
     *
     * @param pngFile on local disk path
     * @throws IOException
     */
    public SheetModel(File pngFile) throws IOException {
        this.path = pngFile.getCanonicalPath();
        log.log(Level.INFO, "SheetModel.Constructor PNG file path is: {0}", pngFile);
        InputStream props = new FileInputStream(
                new File(getPropertiesFileName())
        );
        InputStream imgStreamHiFi;
        try ( InputStream imgStream = new FileInputStream(pngFile)) {
            imgStreamHiFi = new FileInputStream(pngFile);
            initProps(props, imgStream, imgStreamHiFi);
            imgStreamHiFi.close();
        }
    }

    private void initProps(InputStream propsStream, InputStream imgStream, InputStream imgStreamHiFi) throws IOException {
        properties.load(propsStream);
        this.uid = Long.parseUnsignedLong(properties.getProperty("uid", "0"));
        this.size = Integer.parseInt(properties.getProperty("size", "16"));
        this.name = properties.getProperty("name", path.split(".png")[0]);
        this.author = properties.getProperty("author", "Unknown");
        this.width = Integer.parseInt( properties.getProperty("width", "10") );
        this.height = Integer.parseInt( properties.getProperty("height", "10") );
        //this.index = properties.getProperty("index").charAt(0);
        // Put controllable Rectangle over each tile to create a visual context.
        for ( int y = 0; y < getHeight(); y++ ) {
            for ( int x=0; x < getWidth(); x++ ) {
                int tilenum = (getWidth()*y)+x;
                Tile t = new Tile("_" + tilenum, x, y, getPropsFor(tilenum));
                tiles.add(t);
                //TileView tv = new TileView(t, sheet ); //, x, y);
                //tileGroup.getChildren().add(tv);
            }
        }

        this.tileMap = createTileMap(imgStream, imgStreamHiFi);
    }

    private Image createTileMap(InputStream is, InputStream isHiFi) {
        // Get a raw instance of the image to determine its width and height.
        Image image = new Image(is);
        is.mark(Integer.MAX_VALUE);
        this.width = (int) (image.getWidth() / this.size);
        this.height = (int) (image.getHeight() / this.size);

        if (image.getWidth() % this.size != 0) {
            log.log(Level.WARNING,
                    "Image width for sheet ''{0}'' is not a multiple "
                    + "of size:{1}, so it might look ''off''.",
                    new Object[]{this.name, this.size}
            );
        }
        if (image.getHeight() % this.size != 0) {
            log.log(Level.WARNING,
                    "Image height for sheet ''{0}'' is not a multiple "
                    + "of size:{1}, so it might look ''off''.",
                    new Object[]{this.name, this.size}
            );
        }
        return new Image(
                isHiFi,
                this.width * size * fidelity, this.height * size * fidelity,
                true, false // preserve ratio,   smooth        
        );
    }

    public Tile getTile( int i ) {
        return tiles.get(i);
    }
    
    public final String getPropertiesFileName() {
        return path.split(".png")[0] + ".properties";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUID() {
        return uid;
    }
    
    public void setUID( long uid ) {
        this.uid = uid;
    }
    
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getFidelity() {
        return fidelity;
    }

//    public Character getIndex() {
//        return index;
//    }
//
//    public void setIndex(Character c) {
//        this.index = c;
//    }

    /**
     * Dimension of each tile, X or Y.
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Width of sheet in tiles (not pixels)
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Height of sheet in tiles (not pixels)
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    public Image getImage() {
        return tileMap;
    }

//    public TileView getTile( int x, int y ) {
//        return new TileView(getImage(), x, y, getSize(), getFidelity());
//    }
    public String getPath() {
        return path;
    }

    /**
     * For a given sheetFile (PNG), generates and stores a base properties 
     * file in the same folder. Then loads those stored resources and returns
     * a new SheetModel.
     * 
     * @param sheetFile PNG file of Map Sheet
     * @param name Pretty name of the sheet.
     * @param uid
     * @param size of each tile.
     * @param author name of creator of file.
     * @return SheetModel for indicated resources.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static SheetModel createSheet(
            File sheetFile, String name, Long uid, String size, String author
    ) throws FileNotFoundException, IOException {
        Properties sheetProps = new Properties();
        sheetProps.put("name", name);
        sheetProps.put("size", size);
        sheetProps.put("author", author);
        sheetProps.put("uid", uid);

        File propFile = new File(
                sheetFile.getParentFile(),
                sheetFile.getName().split(".png")[0] + ".properties"
        );
        sheetProps.store(new FileOutputStream(propFile), "Created Props");
        log.log(Level.WARNING, "Created properties file for: {0}", propFile.getName());

        return new SheetModel(sheetFile);
    }
    
    public String getPropsFor(int tileNum) {
        return properties.getProperty("flags." + tileNum, "");
    }

}
