package com.svega.vanitygen;

import com.svega.vanitygen.fxmls.LaunchPage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class MoneroVanityGenMain extends Application {

    public static void main(String[] args) {
        if(args.length == 0)
            launch(args);
        else
            VanityGenKt.main(args);
    }

    public static LinkedBlockingQueue<Runnable> q = new LinkedBlockingQueue<>();

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try{
            InputStream in = getClass().getResourceAsStream("fxmls/LaunchPage.fxml");
            root = new FXMLLoader().load(in);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "There was an error loading the application: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        Scene scene = new Scene(root, 1280, 480);

        stage.setTitle("Monero Vanity Address Generator");
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        LaunchPage.stopAll();
    }
}
