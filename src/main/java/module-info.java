module com.pointofsale {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires java.net.http;
    requires java.json;
    requires java.management;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase; 
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;

    opens com.pointofsale.model to javafx.base, com.google.gson;
    opens com.pointofsale to javafx.graphics, javafx.controls;
    exports com.pointofsale.model to com.fasterxml.jackson.databind;
    exports com.pointofsale;
}
