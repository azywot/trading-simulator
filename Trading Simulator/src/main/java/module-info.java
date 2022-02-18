module TradingSimulator.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.math3;

    opens GUI to javafx.fxml;
    exports GUI;
}
