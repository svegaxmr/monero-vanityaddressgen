module monero.vanityaddressgen {
    exports com.svega.vanitygen;
    opens com.svega.vanitygen.fxmls;

    requires java.xml.bind;
    requires javafx.graphics;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.desktop;
    requires javafx.controls;
}