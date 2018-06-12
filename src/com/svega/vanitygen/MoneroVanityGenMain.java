package com.svega.vanitygen;

import com.svega.vanitygen.fxmls.LaunchPage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kotlin.text.Regex;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MoneroVanityGenMain extends Application {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((a, b) -> {
            System.out.println("Thread "+a.getName());
            b.printStackTrace();
            File errFile = new File("err.txt");
            int cnt = 0;
            while(errFile.exists()){
                errFile = new File("err"+(cnt++)+".txt");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(errFile))){
                b.printStackTrace(pw);
                pw.flush();
            } catch (IOException e) {}
        });
        if(args.length == 0)
            launch(args);
        else {
            Scanner in = null;
            char read = 'y';
            if(System.console() != null){
                in = new Scanner(System.in);
                System.out.printf("Matching %s, is this okay? (y/n): ", args[0]);
                read = in.next().charAt(0);
                while(!isYN(read)){
                    System.out.printf("\nInvalid character. Proceed? (y/n): ");
                    read = in.next().charAt(0);
                }
            }
            if(read == 'y')
                VanityGenMain.INSTANCE.cliVanityAddress(args[0], in);
            else
                System.out.println("Not starting the instance.");
            System.exit(0);
        }
    }

    public static long getComplexity(String text) {
        if(text.isEmpty())
            return 1;
        ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());
        char read;
        boolean open = false;
        ArrayList<Regex> regexes = new ArrayList<>();
        String temp = "";
        while ((read = (char) bais.read()) != 65535) {
            switch (read) {
                case '[':
                    if(open)
                        return 0;
                    open = true;
                    temp = "[";
                    break;
                case ']':
                    if(!open)
                        return 0;
                    open = false;
                    temp += read;
                    regexes.add(new Regex(temp));
                    break;
                default:
                    if (open)
                        temp += read;
                    else
                        regexes.add(new Regex(String.valueOf(read)));
            }
        }
        String[] validSeconds = "123456789AB".split("(?!^)");
        String[] validOthers = Base58.Companion.getAlphabetStr().split("(?!^)");
        double pass = 0;
        for (String s : validSeconds) {
            if (regexes.get(0).matches(s))
                ++pass;
        }
        if(pass == 0)
            return 0;
        double diff = validSeconds.length / pass;
        for (Regex r : regexes.subList(1, regexes.size())){
            pass = 0;
            for (String s : validOthers) {
                if (r.matches(s))
                    ++pass;
            }
            if(pass == 0)
                return 0;
            diff *= (validOthers.length / pass);
        }
        return (long)diff;
    }

    public static boolean isYN(char yn){
        return yn == 'y' || yn == 'n';
    }

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try{
            InputStream in = getClass().getResourceAsStream("/com/svega/vanitygen/fxmls/LaunchPage.fxml");
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
