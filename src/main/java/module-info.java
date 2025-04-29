module com.pointofsale {
    requires javafx.controls;
    requires java.net.http;
    requires java.json;
    requires java.management;
    requires java.sql;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;

    opens com.pointofsale.model to javafx.base, com.google.gson;
    exports com.pointofsale.model to com.fasterxml.jackson.databind;
    exports com.pointofsale;
}
