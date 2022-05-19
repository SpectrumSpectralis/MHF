module com.example.javafxtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens org.mhf.mhf.logic to javafx.fxml;
    exports org.mhf.mhf.logic to javafx.graphics;
}