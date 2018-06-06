package com.svega.vanitygen.fxmls;

import com.svega.vanitygen.Base58;
import com.svega.vanitygen.Utils;
import com.svega.vanitygen.VanityGenMain;
import com.svega.vanitygen.VanityGenState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import kotlin.Unit;
import kotlin.text.Regex;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;

public class LaunchPage {
    @FXML
    private TextField regexInput;
    @FXML
    private Label expectedIters, expectedPctEffort, expectedTimeRemaining, numIters, warnText, timeElapsed,
            numGenThreads, numValThreads, addressesPerSecond, status, address, seed, mnemonic, qDepth, postAddressGen;
    @FXML
    private Button start, addWorkerThread, removeWorkerThread, copyMnemonic, copyAddress, copyDonationAddress;

    private static VanityGenState inst;
    private long complexity, elapsedSeconds, addresses;
    private int decimalPlaces = 0;
    private String mnemonicStr, addressStr;

    public LaunchPage(){}
    @FXML
    private void initialize() {
        addWorkerThread.setOnAction(e -> {
            if(inst != null){
                inst.increaseGenThreads();
            }
        });
        removeWorkerThread.setOnAction(e -> {
            if(inst != null){
                inst.decreaseGenThreads();
            }
        });
        copyMnemonic.setOnAction(e -> {//mnemonicStr
            Utils.INSTANCE.copyToClipboard(mnemonicStr);
        });
        copyDonationAddress.setOnAction(e -> {
            Utils.INSTANCE.copyToClipboard("49SVega8pmD5wvb9vai2aC7xQ5vcwbgxfSGm2sEJELoDfx5quMq3b2Rgs9Ua4LfsrTek73fuiatGfEibNvAdS55HABBsJdG");
        });
        copyAddress.setOnAction(e -> {
            Utils.INSTANCE.copyToClipboard(addressStr);
        });
    }
    @FXML
    private void onButtonClicked(){
        String text = regexInput.getText();
        complexity = getComplexity(text);
        if(complexity == 0){
            update(UpdateItem.WARN_TEXT, "Not valid regex for a Monero address!");
        }else {
            update(UpdateItem.COMPLEXITY, new DecimalFormat("#,###").format(complexity));
            update(UpdateItem.ADDRESS, "");
            update(UpdateItem.SEED, "");
            update(UpdateItem.MNEMONIC, "");
            decimalPlaces = (int) Math.log10(complexity) / 2;
            inst = VanityGenMain.INSTANCE.startAsGUI(this, regexInput.getText(), thing -> {
                update(UpdateItem.ADDRESS, thing.getFirst());
                update(UpdateItem.SEED, thing.getSecond());
                return Unit.INSTANCE;
            });
        }
    }

    private long getComplexity(String text) {
        if(text.isEmpty())
            return 1;
        ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());
        char read;
        boolean open = false;
        ArrayList<Regex> regexes = new ArrayList<>();
        String temp = "";
        System.out.println("Pre-read");
        while ((read = (char) bais.read()) != 65535) {
            System.out.printf("read %s\n", String.valueOf(read));
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
        System.out.println("Post-read");
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

    public static void stopAll(){
        if(inst != null)
            inst.stop();
    }
    public void update(UpdateItem item, Object in){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch(item){
                    case NUMBER_GEN:
                        addresses = (int)in;
                        numIters.setText("Iterations so far: "+new DecimalFormat("#,###").format(in));
                        double exp = 100 * ((double)addresses) / ((double)complexity);
                        String pctEff = String.format("%1."+decimalPlaces+"f%%", exp);
                        expectedPctEffort.setText("Percentage effort based on expectations: " + pctEff);
                        break;
                    case WARN_TEXT:
                        warnText.setText((String)in);
                        break;
                    case VAL_THREADS:
                        numValThreads.setText("Number of validation threads: "+in);
                        break;
                    case GEN_THREADS:
                        numGenThreads.setText("Number of generation threads: "+in);
                        break;
                    case ADDRESS:
                        addressStr = (String)in;
                        address.setText("Address is: "+in);
                        break;
                    case SEED:
                        seed.setText("Seed is: "+in);
                        break;
                    case ADDRESSES_PER_SEC:
                        addressesPerSecond.setText("Addresses generated per second: "+in);
                        long aps = (long) in;
                        long expectedSecs = ((complexity - addresses) / aps) + elapsedSeconds;
                        expectedTimeRemaining.setText("Expected to take "+Utils.INSTANCE.formatDuration(Duration.ofSeconds(expectedSecs)));
                        break;
                    case STATUS:
                        status.setText((String)in);
                        break;
                    case TIME:
                        elapsedSeconds = (Duration.ofSeconds((long)in)).getSeconds();
                        timeElapsed.setText("Time elapsed: "+Utils.INSTANCE.formatDuration(Duration.ofSeconds((long)in)));
                        break;
                    case COMPLEXITY:
                        expectedIters.setText("Expected number of iterations to make: "+in);
                        break;
                    case MNEMONIC:
                        mnemonicStr = (String)in;
                        mnemonic.setText("Mnemonic is: "+in);
                        break;
                    case QDEPTH:
                        qDepth.setText("Queue depth: "+in);
                        break;
                    case POST_GEN:
                        postAddressGen.setText((String)in);
                        break;
                }
            }
        });
    }
    public enum UpdateItem{
        TIME,
        NUMBER_GEN,
        VAL_THREADS,
        GEN_THREADS,
        WARN_TEXT,
        ADDRESS,
        SEED,
        MNEMONIC,
        ADDRESSES_PER_SEC,
        STATUS,
        COMPLEXITY,
        QDEPTH,
        POST_GEN
    }
}
