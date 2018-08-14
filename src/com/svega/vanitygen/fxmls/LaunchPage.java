package com.svega.vanitygen.fxmls;

import com.svega.common.utils.SystemUtils;
import com.svega.common.utils.TimeUtils;
import com.svega.vanitygen.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import kotlin.Unit;

import java.text.DecimalFormat;
import java.time.Duration;

public class LaunchPage implements ProgressUpdatable {
    @FXML
    private TextField regexInput;
    @FXML
    private Label expectedIters, expectedPctEffort, expectedTimeRemaining, numIters, warnText, timeElapsed, version,
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
        version.setText("Version "+Utils.INSTANCE.getVERSION_STRING());
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
            SystemUtils.INSTANCE.copyToClipboard(mnemonicStr);
        });
        copyDonationAddress.setOnAction(e -> {
            SystemUtils.INSTANCE.copyToClipboard(Utils.DONATION_ADDRESS);
        });
        copyAddress.setOnAction(e -> {
            SystemUtils.INSTANCE.copyToClipboard(addressStr);
        });
    }
    @FXML
    private void onButtonClicked(){
        if(inst != null && inst.isWorking()){
            inst.stop();
            start.setText("Begin matching");
            status.setText("Stopped");
        }else {
            String text = regexInput.getText();
            complexity = MoneroVanityGenMain.getComplexity(text);
            if (complexity <= 0) {
                update(UpdateItem.WARN_TEXT, "Not valid regex for a Monero address!");
            } else {
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
            start.setText("Stop working");
        }
    }

    public static void stopAll(){
        if(inst != null)
            inst.stop();
    }
    @Override
    public void update(UpdateItem item, Object in){
        Platform.runLater(() -> {
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
                    start.setText("Stop working");
                    addressesPerSecond.setText("Addresses generated per second: "+in);
                    long aps = Math.max((long) in, 1);
                    long expectedSecs = ((complexity - addresses) / aps) + elapsedSeconds;
                    expectedTimeRemaining.setText("Expected to take "+TimeUtils.INSTANCE.formatDuration(Duration.ofSeconds(expectedSecs)));
                    break;
                case STATUS:
                    status.setText((String)in);
                    break;
                case TIME:
                    elapsedSeconds = (Duration.ofSeconds((long)in)).getSeconds();
                    timeElapsed.setText("Time elapsed: "+TimeUtils.INSTANCE.formatDuration(Duration.ofSeconds((long)in)));
                    break;
                case COMPLEXITY:
                    expectedIters.setText("Expected number of iterations to make: "+in);
                    break;
                case MNEMONIC:
                    mnemonicStr = (String)in;
                    mnemonic.setText("Mnemonic is: "+in);
                    start.setText("Begin matching");
                    break;
                case QDEPTH:
                    qDepth.setText("Queue depth: "+in);
                    break;
                case POST_GEN:
                    postAddressGen.setText((String)in);
                    break;
            }
        });
    }
}
