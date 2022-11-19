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

package com.maehem.deeps;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Mark J Koch ( @maehem on GitHub )
 */
public class Deeps  extends Application {
    public static final Logger log = Logger.getLogger("deeps");

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private Stage window;
    private Group playArea = new Group();
    private final StackPane topArea = new StackPane(playArea);
    private final StackPane root = new StackPane(topArea);
    private final Scene scene = new Scene(root); //, 1280, 920);

    static {
          System.setProperty("java.util.logging.SimpleFormatter.format",
                  "[%1$tF %1$tT %1$tL] [%4$-7s] %5$s%n");
      }

 
    @Override
    public void start(Stage window) throws Exception {
        ConsoleHandler handler
                    = new ConsoleHandler();

        // Add console handler as handler of logs
        log.addHandler(handler);

        log.setLevel(Level.FINER);
        for (Handler h : log.getHandlers()) {
            h.setLevel(Level.FINER);
        }

        this.window = window;
        window.setScene(this.scene);
        window.setTitle("Goin Down in the Deeps...");
        window.setResizable(false);
        window.setWidth(WIDTH);
        window.setHeight(HEIGHT);
        
        //quit when the window is close().
        window.setOnCloseRequest(e -> Platform.exit());

        // Maybe catch exception here to report what kind?
        //playArea.getChildren().add(new StartingZone());
        
        window.show();        
    }

    public static void main(String[] args) {
        launch(args);
    }

}
