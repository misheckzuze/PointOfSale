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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Locale;
import java.text.NumberFormat;
import com.pointofsale.model.Product;
import javafx.stage.StageStyle;
import com.pointofsale.helper.Helper;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    
    // Enhanced UI Constants
    private static final String DIALOG_STYLE = "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);";
    private static final String TOOLBAR_STYLE = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);";
    private static final String BUTTON_BAR_STYLE = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, -1);";
    private static final String RECEIPT_STYLE = "-fx-background-color: white; -fx-padding: 25; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 2); -fx-background-radius: 5; -fx-border-radius: 5;";
    private static final String PREVIEW_STYLE = "-fx-background-color: #f5f5f7; -fx-background: #f5f5f7;";
    private static final String BUTTON_STYLE = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 8 16;";
    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: linear-gradient(to bottom, #4f46e5, #3730a3); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);";
    
    private static final int RECEIPT_WIDTH = 320;
    private static final int SEPARATOR_LENGTH = 70;
    
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
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle(DIALOG_STYLE);
        
        mainLayout.setTop(createEnhancedToolbar());
        mainLayout.setCenter(createPreviewArea());
        mainLayout.setBottom(createEnhancedButtonBar());
        
        Scene scene = new Scene(mainLayout, 850, 950);
        stage.setScene(scene);
        
        generateModernReceiptContent();
    }
    
    private HBox createEnhancedToolbar() {
        HBox toolbar = new HBox(20);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(TOOLBAR_STYLE);
        
        Label settingsLabel = createStyledLabel("Print Settings:", "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #374151;");
        
        // Enhanced scale control
        Label scaleLabel = createStyledLabel("Scale:", "-fx-font-weight: 500; -fx-text-fill: #6b7280;");
        Slider scaleSlider = createEnhancedScaleSlider();
        Label scaleValueLabel = new Label("100%");
        scaleValueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4f46e5; -fx-min-width: 40;");
        
        // Enhanced checkboxes
        CheckBox logoCheckbox = createEnhancedCheckbox("Include Logo", includeLogo, this::onLogoToggle);
        CheckBox customerCopyCheckbox = createEnhancedCheckbox("Customer Copy", includeCustomerCopy, this::onCustomerCopyToggle);
        
        Button pageSetupBtn = createButton("Page Setup", BUTTON_STYLE, this::showPageSetup);
        
        Separator[] separators = createStyledSeparators(3);
        
        // Update scale value label
        scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        toolbar.getChildren().addAll(
            settingsLabel, separators[0],
            scaleLabel, scaleSlider, scaleValueLabel,
            separators[1], logoCheckbox, customerCopyCheckbox,
            separators[2], pageSetupBtn
        );
        
        return toolbar;
    }
    
    private Slider createEnhancedScaleSlider() {
        Slider slider = new Slider(0.6, 2.0, 1.0);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.2);
        slider.setPrefWidth(140);
        slider.setStyle("-fx-control-inner-background: #f3f4f6;");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            printScale = newVal.doubleValue();
            updatePreview();
        });
        return slider;
    }
    
    private CheckBox createEnhancedCheckbox(String text, boolean selected, Runnable action) {
        CheckBox checkbox = new CheckBox(text);
        checkbox.setSelected(selected);
        checkbox.setStyle("-fx-font-weight: 500; -fx-text-fill: #374151;");
        checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> action.run());
        return checkbox;
    }
    
    private Separator[] createStyledSeparators(int count) {
        Separator[] separators = new Separator[count];
        for (int i = 0; i < count; i++) {
            separators[i] = new Separator(javafx.geometry.Orientation.VERTICAL);
            separators[i].setPrefHeight(30);
            separators[i].setStyle("-fx-background-color: #dee2e6;");
        }
        return separators;
    }
    
    private ScrollPane createPreviewArea() {
        previewPane = new ScrollPane();
        previewPane.setStyle(PREVIEW_STYLE);
        previewPane.setFitToWidth(true);
        previewPane.setPadding(new Insets(25));
        return previewPane;
    }
    
    private HBox createEnhancedButtonBar() {
        HBox buttonBar = new HBox(15);
        buttonBar.setPadding(new Insets(20));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setStyle(BUTTON_BAR_STYLE);
        
        Button cancelButton = createButton("Cancel", BUTTON_STYLE, stage::close);
        cancelButton.setPrefWidth(110);
        
        Button printButton = createButton("Print Receipt", PRIMARY_BUTTON_STYLE, this::printReceipt);
        printButton.setPrefWidth(140);
        
        buttonBar.getChildren().addAll(cancelButton, printButton);
        return buttonBar;
    }
    
    private void generateModernReceiptContent() {
        receiptContent = new VBox(3);
        receiptContent.setAlignment(Pos.TOP_CENTER);
        receiptContent.setStyle(RECEIPT_STYLE);
        receiptContent.setPrefWidth(RECEIPT_WIDTH);
        receiptContent.setMaxWidth(RECEIPT_WIDTH);
        
        if (includeLogo) {
            receiptContent.getChildren().add(createModernStoreHeader());
        }
        
        receiptContent.getChildren().addAll(
            createModernReceiptInfo(),
            createModernItemsSection(),
            createModernTotalsSection(),
            createModernFooter()
        );
        
        if (includeCustomerCopy) {
            receiptContent.getChildren().add(createCustomerCopyNotice());
        }
        
        updatePreview();
    }
    
    private VBox createModernStoreHeader() {
        String tradingName = Helper.getTrading();
        String storeInfo = "123 Main Street\nCity, State 12345\nPhone: (555) 123-4567";
        
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        // Company name - large and prominent
        Text companyName = new Text(tradingName);
        companyName.setFont(Font.font("System", FontWeight.BOLD, 20));
        companyName.setStyle("-fx-fill: #1f2937;");
        
        // Store info with better styling
        Text storeDetails = new Text(storeInfo);
        storeDetails.setFont(Font.font("System", 11));
        storeDetails.setStyle("-fx-fill: #6b7280;");
        storeDetails.setTextAlignment(TextAlignment.CENTER);
        
        // Elegant separator
        Text separator = createStyledSeparator("═", "#e5e7eb");
        
        header.getChildren().addAll(companyName, storeDetails, separator);
        return header;
    }
    
    private VBox createModernReceiptInfo() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        
        VBox infoSection = new VBox(5);
        infoSection.setPadding(new Insets(0, 0, 15, 0));
        
        // Receipt title
        Text receiptTitle = new Text("TAX INVOICE");
        receiptTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        receiptTitle.setStyle("-fx-fill: #374151;");
        
        VBox titleBox = new VBox(receiptTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 10, 0));
        
        // Transaction details with modern formatting
        VBox details = new VBox(3);
        details.getChildren().addAll(
            createDetailRow("Receipt No:", receiptNumber),
            createDetailRow("Date:", now.format(formatter)),
            createDetailRow("Cashier:", cashierName)
        );
        
        Text separator = createStyledSeparator("─", "#d1d5db");
        
        infoSection.getChildren().addAll(titleBox, details, separator);
        return infoSection;
    }
    
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Text labelText = new Text(label);
        labelText.setFont(Font.font("System", FontWeight.NORMAL, 11));
        labelText.setStyle("-fx-fill: #6b7280;");
        
        Text valueText = new Text(value);
        valueText.setFont(Font.font("System", FontWeight.NORMAL, 11));
        valueText.setStyle("-fx-fill: #374151;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }
    
    private VBox createModernItemsSection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(0, 0, 15, 0));
        
        // Items header
        Text itemsHeader = new Text("ITEMS PURCHASED");
        itemsHeader.setFont(Font.font("System", FontWeight.BOLD, 12));
        itemsHeader.setStyle("-fx-fill: #374151;");
        
        VBox headerBox = new VBox(itemsHeader);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 8, 0));
        
        // Column headers
        HBox columnHeaders = createColumnHeaders();
        Text headerSeparator = createStyledSeparator("·", "#d1d5db");
        
        // Items list
        VBox itemsList = new VBox(2);
        for (Product item : cartItems) {
            itemsList.getChildren().add(createModernItemRow(item));
        }
        
        Text footerSeparator = createStyledSeparator("─", "#d1d5db");
        
        section.getChildren().addAll(headerBox, columnHeaders, headerSeparator, itemsList, footerSeparator);
        return section;
    }
    
    private HBox createColumnHeaders() {
        HBox headers = new HBox();
        headers.setAlignment(Pos.CENTER_LEFT);
        
        Text itemHeader = new Text("ITEM");
        itemHeader.setFont(Font.font("System", FontWeight.BOLD, 10));
        itemHeader.setStyle("-fx-fill: #6b7280;");
        
        Text qtyHeader = new Text("QTY");
        qtyHeader.setFont(Font.font("System", FontWeight.BOLD, 10));
        qtyHeader.setStyle("-fx-fill: #6b7280;");
        
        Text priceHeader = new Text("PRICE");
        priceHeader.setFont(Font.font("System", FontWeight.BOLD, 10));
        priceHeader.setStyle("-fx-fill: #6b7280;");
        
        Text totalHeader = new Text("TOTAL");
        totalHeader.setFont(Font.font("System", FontWeight.BOLD, 10));
        totalHeader.setStyle("-fx-fill: #6b7280;");
        
        // Spacing regions
        Region spacer1 = new Region();
        spacer1.setPrefWidth(120);
        Region spacer2 = new Region();
        spacer2.setPrefWidth(40);
        Region spacer3 = new Region();
        spacer3.setPrefWidth(50);
        
        headers.getChildren().addAll(itemHeader, spacer1, qtyHeader, spacer2, priceHeader, spacer3, totalHeader);
        return headers;
    }
    
    private VBox createModernItemRow(Product item) {
        VBox itemRow = new VBox(1);
        
        HBox mainRow = new HBox();
        mainRow.setAlignment(Pos.CENTER_LEFT);
        
        // Item name (truncated if necessary)
        String itemName = item.getName().length() > 18 ? 
            item.getName().substring(0, 15) + "..." : item.getName();
        
        Text nameText = new Text(itemName);
        nameText.setFont(Font.font("System", FontWeight.NORMAL, 11));
        nameText.setStyle("-fx-fill: #374151;");
        
        Text qtyText = new Text(String.valueOf((int) item.getQuantity()));
        qtyText.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        qtyText.setStyle("-fx-fill: #374151;");
        
        Text priceText = new Text(formatCurrency(item.getPrice()));
        priceText.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        priceText.setStyle("-fx-fill: #374151;");
        
        Text totalText = new Text(formatCurrency(item.getTotal()));
        totalText.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        totalText.setStyle("-fx-fill: #374151;");
        
        // Spacing
        Region spacer1 = new Region();
        spacer1.setPrefWidth(105);
        Region spacer2 = new Region();
        spacer2.setPrefWidth(30);
        Region spacer3 = new Region();
        spacer3.setPrefWidth(35);
        
        mainRow.getChildren().addAll(nameText, spacer1, qtyText, spacer2, priceText, spacer3, totalText);
        itemRow.getChildren().add(mainRow);
        
        // Show full name if truncated
        if (item.getName().length() > 18) {
            Text fullName = new Text("  " + item.getName());
            fullName.setFont(Font.font("System", FontWeight.NORMAL, 9));
            fullName.setStyle("-fx-fill: #9ca3af;");
            itemRow.getChildren().add(fullName);
        }
        
        return itemRow;
    }
    
    private VBox createModernTotalsSection() {
        VBox section = new VBox(4);
        section.setPadding(new Insets(15, 0, 15, 0));
        
        // Subtotal
        section.getChildren().add(createTotalRow("Subtotal:", formatCurrency(subtotal), false));
        
        // Tax
        section.getChildren().add(createTotalRow("Tax:", formatCurrency(tax), false));
        
        // Separator before grand total
        Text separator = createStyledSeparator("─", "#d1d5db");
        section.getChildren().add(separator);
        
        // Grand total (emphasized)
        section.getChildren().add(createTotalRow("TOTAL:", formatCurrency(total), true));
        
        // Bottom separator
        Text bottomSeparator = createStyledSeparator("═", "#e5e7eb");
        section.getChildren().add(bottomSeparator);
        
        return section;
    }
    
    private HBox createTotalRow(String label, String amount, boolean isGrandTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Text labelText = new Text(label);
        Text amountText = new Text(amount);
        
        if (isGrandTotal) {
            labelText.setFont(Font.font("System", FontWeight.BOLD, 14));
            labelText.setStyle("-fx-fill: #1f2937;");
            amountText.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
            amountText.setStyle("-fx-fill: #1f2937;");
        } else {
            labelText.setFont(Font.font("System", FontWeight.NORMAL, 12));
            labelText.setStyle("-fx-fill: #374151;");
            amountText.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
            amountText.setStyle("-fx-fill: #374151;");
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        row.getChildren().addAll(labelText, spacer, amountText);
        return row;
    }
    
    private VBox createModernFooter() {
        VBox footer = new VBox(8);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));
        
        Text thankYou = new Text("THANK YOU FOR YOUR BUSINESS!");
        thankYou.setFont(Font.font("System", FontWeight.BOLD, 13));
        thankYou.setStyle("-fx-fill: #1f2937;");
        
        Text returnPolicy = new Text("Returns accepted within 30 days with receipt");
        returnPolicy.setFont(Font.font("System", FontWeight.NORMAL, 10));
        returnPolicy.setStyle("-fx-fill: #6b7280;");
        
        Text warranty = new Text("Keep this receipt for warranty purposes");
        warranty.setFont(Font.font("System", FontWeight.NORMAL, 10));
        warranty.setStyle("-fx-fill: #6b7280;");
        
        footer.getChildren().addAll(thankYou, returnPolicy, warranty);
        return footer;
    }
    
    private VBox createCustomerCopyNotice() {
        VBox notice = new VBox(5);
        notice.setAlignment(Pos.CENTER);
        notice.setPadding(new Insets(20, 0, 0, 0));
        
        Text separator = createStyledSeparator("┄", "#d1d5db");
        
        Text copyText = new Text("CUSTOMER COPY");
        copyText.setFont(Font.font("System", FontWeight.BOLD, 11));
        copyText.setStyle("-fx-fill: #6b7280;");
        
        notice.getChildren().addAll(separator, copyText);
        return notice;
    }
    
    // Helper Methods
    
    private Text createStyledSeparator(String character, String color) {
        Text separator = new Text(character.repeat(SEPARATOR_LENGTH));
        separator.setFont(Font.font("Monospace", 8));
        separator.setStyle("-fx-fill: " + color + ";");
        return separator;
    }
    
    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }
    
    private Button createButton(String text, String style, Runnable action) {
        Button button = new Button(text);
        button.setStyle(style);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    private void onLogoToggle() {
        includeLogo = !includeLogo;
        generateModernReceiptContent();
    }
    
    private void onCustomerCopyToggle() {
        includeCustomerCopy = !includeCustomerCopy;
        generateModernReceiptContent();
    }
    
    private void updatePreview() {
        receiptContent.setScaleX(printScale);
        receiptContent.setScaleY(printScale);
        
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.getChildren().add(receiptContent);
        
        previewPane.setContent(container);
    }
    
    private void printReceipt() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        
        if (printerJob != null) {
            boolean proceed = printerJob.showPrintDialog(stage);
            
            if (proceed) {
                Printer printer = printerJob.getPrinter();
                PageLayout pageLayout = printer.createPageLayout(
                    Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
                
                // Temporarily adjust scale for printing
                double originalScale = printScale;
                receiptContent.setScaleX(0.8);
                receiptContent.setScaleY(0.8);
                
                boolean success = printerJob.printPage(pageLayout, receiptContent);
                
                // Restore original scale
                receiptContent.setScaleX(originalScale);
                receiptContent.setScaleY(originalScale);
                
                if (success) {
                    printerJob.endJob();
                    showAlert("Print Successful", "Receipt has been sent to printer successfully.", Alert.AlertType.INFORMATION);
                    stage.close();
                } else {
                    showAlert("Print Error", "Failed to print receipt. Please try again.", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Print Error", "No printer available. Please check your printer connection.", Alert.AlertType.ERROR);
        }
    }
    
    private void showPageSetup() {
        showAlert("Page Setup", "Receipt will be printed on standard paper size.\n" +
                "For thermal printers, ensure correct paper size is selected in printer settings.", 
                Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatCurrency(double amount) {
      Locale malawiLocale = new Locale.Builder().setLanguage("en").setRegion("MW").build();
      NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(malawiLocale);
      return currencyFormatter.format(amount);
    }
    public void show() {
        stage.show();
    }
    
    public void showAndWait() {
        stage.showAndWait();
    }
}