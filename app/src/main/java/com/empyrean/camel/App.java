package com.empyrean.camel;

import org.apache.camel.main.Main;

public class App {

    public static void main(String[] args) throws Exception {
        // Create a Camel Main instance
        Main main = new Main(App.class);

        // Start the application
        main.run(args);

    }
}

