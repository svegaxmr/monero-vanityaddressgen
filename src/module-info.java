module monero.vanityaddressgen {
    exports com.svega.vanitygen;
    opens com.svega.vanitygen.fxmls;

    requires javafx.graphics;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.desktop;
    requires javafx.controls;
    requires jvm.ed25519;
    requires jvm.crypto;
    requires svega.common.utils;
    requires monero.utils.core;
}