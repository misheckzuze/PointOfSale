module com.pointofsale {
    requires javafx.controls;
    requires java.net.http;
    requires java.json;
    requires java.management;
    requires java.sql;
    requires com.google.gson;

    opens com.pointofsale.model to javafx.base, com.google.gson;
    exports com.pointofsale;
}
