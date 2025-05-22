package com.pointofsale;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.pointofsale.model.Product;
import javafx.stage.StageStyle;
import com.pointofsale.helper.Helper;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Print Preview Dialog for POS System Receipt
 * Provides a professional print preview with formatting options
 */
public class PrintPreviewDialog {
    
    private Stage stage;
    private Stage parentStage;
    private ObservableList<Product> cartItems;
    private double subtotal;
    private double tax;
    private double total;
    private String receiptNumber;
    private String cashierName;
    private VBox receiptContent;
    private ScrollPane previewPane;
    
    // Print settings
    private double printScale = 1.0;
    private boolean includeLogo = true;
    private boolean includeCustomerCopy = true;
    
    public PrintPreviewDialog(Stage parentStage, ObservableList<Product> cartItems, 
                            double subtotal, double tax, double total, 
                            String receiptNumber, String cashierName) {
        this.parentStage = parentStage;
        this.cartItems = cartItems;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.receiptNumber = receiptNumber;
        this.cashierName = cashierName;
        
        initializeDialog();
    }
    
    private void initializeDialog() {
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Print Preview - Receipt #" + receiptNumber);
        stage.setResizable(true);
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f7;");
        
        // Create toolbar
        mainLayout.setTop(createToolbar());
        
        // Create preview area
        mainLayout.setCenter(createPreviewArea());
        
        // Create bottom button bar
        mainLayout.setBottom(createButtonBar());
        
        // Create scene
        Scene scene = new Scene(mainLayout, 800, 900);
        stage.setScene(scene);
        
        // Generate initial receipt content
        generateReceiptContent();
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 15, 10, 15));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Print settings label
        Label settingsLabel = new Label("Print Settings:");
        settingsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Scale control
        Label scaleLabel = new Label("Scale:");
        Slider scaleSlider = new Slider(0.5, 2.0, 1.0);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setMajorTickUnit(0.5);
        scaleSlider.setPrefWidth(120);
        scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            printScale = newVal.doubleValue();
            updatePreview();
        });
        
        Label scaleValueLabel = new Label("100%");
        scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        // Include logo checkbox
        CheckBox logoCheckbox = new CheckBox("Include Logo");
        logoCheckbox.setSelected(includeLogo);
        logoCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            includeLogo = newVal;
            generateReceiptContent();
        });
        
        // Customer copy checkbox
        CheckBox customerCopyCheckbox = new CheckBox("Customer Copy");
        customerCopyCheckbox.setSelected(includeCustomerCopy);
        customerCopyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            includeCustomerCopy = newVal;
            generateReceiptContent();
        });
        
        // Separator
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(25);
        
        // Page setup button
        Button pageSetupBtn = new Button("Page Setup");
        pageSetupBtn.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
        pageSetupBtn.setOnAction(e -> showPageSetup());
        
        toolbar.getChildren().addAll(
            settingsLabel, new Separator(javafx.geometry.Orientation.VERTICAL),
            scaleLabel, scaleSlider, scaleValueLabel,
            sep, logoCheckbox, customerCopyCheckbox,
            new Separator(javafx.geometry.Orientation.VERTICAL), pageSetupBtn
        );
        
        return toolbar;
    }
    
    private ScrollPane createPreviewArea() {
        previewPane = new ScrollPane();
        previewPane.setStyle("-fx-background-color: #e8e8e8; -fx-background: #e8e8e8;");
        previewPane.setFitToWidth(true);
        previewPane.setPadding(new Insets(20));
        
        return previewPane;
    }
    
    private HBox createButtonBar() {
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> stage.close());
        
        Button printButton = new Button("Print Receipt");
        printButton.setPrefWidth(120);
        printButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        printButton.setOnAction(e -> printReceipt());
        
        buttonBar.getChildren().addAll(cancelButton, printButton);
        
        return buttonBar;
    }
    
    private void generateReceiptContent() {
        receiptContent = new VBox();
        receiptContent.setAlignment(Pos.TOP_CENTER);
        receiptContent.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #ccc; -fx-border-width: 1;");
        receiptContent.setPrefWidth(300); // Standard receipt width
        receiptContent.setMaxWidth(300);
        
        // Store header
        if (includeLogo) {
            VBox header = createStoreHeader();
            receiptContent.getChildren().add(header);
        }
        
        // Receipt info
        VBox receiptInfo = createReceiptInfo();
        receiptContent.getChildren().add(receiptInfo);
        
        // Items section
        VBox itemsSection = createItemsSection();
        receiptContent.getChildren().add(itemsSection);
        
        // Totals section
        VBox totalsSection = createTotalsSection();
        receiptContent.getChildren().add(totalsSection);
        
        // Footer
        VBox footer = createFooter();
        receiptContent.getChildren().add(footer);
        
        // Customer copy notice
        if (includeCustomerCopy) {
            VBox customerCopy = createCustomerCopyNotice();
            receiptContent.getChildren().add(customerCopy);
        }
        
        updatePreview();
    }
    
    private VBox createStoreHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        String tradingName = Helper.getTrading(); // Assuming this method exists
        Text storeName = new Text(tradingName);
        storeName.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Text storeAddress = new Text("123 Main Street\nCity, State 12345\nPhone: (555) 123-4567");
        storeAddress.setFont(Font.font("System", 10));
        storeAddress.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        // Separator line
        Text separator = new Text("━".repeat(35));
        separator.setFont(Font.font("Monospace", 8));
        
        header.getChildren().addAll(storeName, storeAddress, separator);
        
        return header;
    }
    
    private VBox createReceiptInfo() {
        VBox info = new VBox(3);
        info.setPadding(new Insets(0, 0, 10, 0));
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        
        Text receiptNumText = new Text("Receipt #: " + receiptNumber);
        receiptNumText.setFont(Font.font("Monospace", 10));
        
        Text dateText = new Text("Date: " + now.format(formatter));
        dateText.setFont(Font.font("Monospace", 10));
        
        Text cashierText = new Text("Cashier: " + cashierName);
        cashierText.setFont(Font.font("Monospace", 10));
        
        Text separator = new Text("━".repeat(35));
        separator.setFont(Font.font("Monospace", 8));
        
        info.getChildren().addAll(receiptNumText, dateText, cashierText, separator);
        
        return info;
    }
    
    private VBox createItemsSection() {
        VBox itemsSection = new VBox(2);
        itemsSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Header
        Text header = new Text(String.format("%-15s %3s %8s %8s", "ITEM", "QTY", "PRICE", "TOTAL"));
        header.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        
        Text headerSeparator = new Text("─".repeat(35));
        headerSeparator.setFont(Font.font("Monospace", 8));
        
        itemsSection.getChildren().addAll(header, headerSeparator);
       
        // Items
        for (Product item : cartItems) {
            String itemName = item.getName();
            if (itemName.length() > 15) {
                itemName = itemName.substring(0, 12) + "...";
            }
            
            int quantity = (int) item.getQuantity();
            
            Text itemLine = new Text(String.format("%-15s %3d %8s %8s",
                itemName,
                quantity,
                formatCurrency(item.getPrice()),
                formatCurrency(item.getTotal())
            ));
            itemLine.setFont(Font.font("Monospace", 9));
            
            itemsSection.getChildren().add(itemLine);
        }
        
        return itemsSection;
    }
    
    private VBox createTotalsSection() {
        VBox totalsSection = new VBox(2);
        totalsSection.setPadding(new Insets(10, 0, 10, 0));
        
        Text separator = new Text("━".repeat(35));
        separator.setFont(Font.font("Monospace", 8));
        
        Text subtotalLine = new Text(String.format("%20s: %10s", "Subtotal", formatCurrency(subtotal)));
        subtotalLine.setFont(Font.font("Monospace", 10));
        
        Text taxLine = new Text(String.format("%20s: %10s", "Tax", formatCurrency(tax)));
        taxLine.setFont(Font.font("Monospace", 10));
        
        Text totalSeparator = new Text("═".repeat(35));
        totalSeparator.setFont(Font.font("Monospace", 8));
        
        Text totalLine = new Text(String.format("%20s: %10s", "TOTAL", formatCurrency(total)));
        totalLine.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        
        totalsSection.getChildren().addAll(separator, subtotalLine, taxLine, totalSeparator, totalLine);
        
        return totalsSection;
    }
    
    private VBox createFooter() {
        VBox footer = new VBox(3);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15, 0, 0, 0));
        
        Text thankYou = new Text("Thank you for your business!");
        thankYou.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        Text returnPolicy = new Text("Returns accepted within 30 days with receipt");
        returnPolicy.setFont(Font.font("System", 8));
        
        footer.getChildren().addAll(thankYou, returnPolicy);
        
        return footer;
    }
    
    private VBox createCustomerCopyNotice() {
        VBox notice = new VBox(5);
        notice.setAlignment(Pos.CENTER);
        notice.setPadding(new Insets(20, 0, 0, 0));
        
        Text separator = new Text("┄".repeat(35));
        separator.setFont(Font.font("Monospace", 8));
        
        Text copyNotice = new Text("CUSTOMER COPY");
        copyNotice.setFont(Font.font("System", FontWeight.BOLD, 10));
        
        notice.getChildren().addAll(separator, copyNotice);
        
        return notice;
    }
    
    private void updatePreview() {
        // Apply scale transformation
        receiptContent.setScaleX(printScale);
        receiptContent.setScaleY(printScale);
        
        // Wrap in container to handle scaling
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(receiptContent);
        
        previewPane.setContent(container);
    }
    
    private void printReceipt() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        
        if (printerJob != null) {
            // Show print dialog
            boolean proceed = printerJob.showPrintDialog(stage);
            
            if (proceed) {
                // Configure page layout for receipt
                Printer printer = printerJob.getPrinter();
                PageLayout pageLayout = printer.createPageLayout(
                    Paper.A4, 
                    PageOrientation.PORTRAIT, 
                    Printer.MarginType.HARDWARE_MINIMUM
                );
                
                // Reset scale for actual printing
                receiptContent.setScaleX(0.8); // Smaller scale for printing
                receiptContent.setScaleY(0.8);
                
                boolean success = printerJob.printPage(pageLayout, receiptContent);
                
                if (success) {
                    printerJob.endJob();
                    showPrintSuccess();
                    stage.close();
                } else {
                    showPrintError("Failed to print receipt. Please try again.");
                }
                
                // Restore preview scale
                receiptContent.setScaleX(printScale);
                receiptContent.setScaleY(printScale);
            }
        } else {
            showPrintError("No printer available. Please check your printer connection.");
        }
    }
    
    private void showPageSetup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Page Setup");
        alert.setHeaderText("Print Configuration");
        alert.setContentText("Receipt will be printed on standard paper size.\n" +
                           "For thermal printers, ensure correct paper size is selected in printer settings.");
        alert.showAndWait();
    }
    
    private void showPrintSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Successful");
        alert.setHeaderText(null);
        alert.setContentText("Receipt has been sent to printer successfully.");
        alert.showAndWait();
    }
    
    private void showPrintError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Print Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
    
    public void show() {
        stage.show();
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}

