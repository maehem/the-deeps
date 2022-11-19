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

import static com.maehem.deeps.Deeps.log;
import com.maehem.deeps.editor.EditorMainView;
import java.util.logging.Handler;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Mark J Koch
 */
public class DeepEdit extends Application {

    @Override
    public void start(Stage window) throws Exception {
//        ConsoleHandler handler
//                    = new ConsoleHandler();

        // Add console handler as handler of logs
        //log.setUseParentHandlers(false);
        //log.addHandler(handler);

        log.setLevel(Level.FINER);
        for (Handler h : log.getHandlers()) {
            h.setLevel(Level.FINER);
        }

        new EditorMainView(window);        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
