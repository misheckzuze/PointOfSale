package com.pointofsale;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ReportsView {

    // Mock data classes
    public static class SaleSummary {
        private final SimpleStringProperty date;
        private final SimpleIntegerProperty transactions;
        private final SimpleDoubleProperty revenue;
        private final SimpleDoubleProperty tax;

        public SaleSummary(String date, int transactions, double revenue, double tax) {
            this.date = new SimpleStringProperty(date);
            this.transactions = new SimpleIntegerProperty(transactions);
            this.revenue = new SimpleDoubleProperty(revenue);
            this.tax = new SimpleDoubleProperty(tax);
        }

        public String getDate() { return date.get(); }
        public int getTransactions() { return transactions.get(); }
        public double getRevenue() { return revenue.get(); }
        public double getTax() { return tax.get(); }
    }

    public static class ProductSale {
        private final SimpleStringProperty productName;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty revenue;
        private final SimpleDoubleProperty profit;

        public ProductSale(String productName, int quantity, double revenue, double profit) {
            this.productName = new SimpleStringProperty(productName);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.revenue = new SimpleDoubleProperty(revenue);
            this.profit = new SimpleDoubleProperty(profit);
        }

        public String getProductName() { return productName.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getRevenue() { return revenue.get(); }
        public double getProfit() { return profit.get(); }
    }

    public static class TaxSummary {
        private final SimpleStringProperty taxRate;
        private final SimpleDoubleProperty totalSales;
        private final SimpleDoubleProperty taxAmount;

        public TaxSummary(String taxRate, double totalSales, double taxAmount) {
            this.taxRate = new SimpleStringProperty(taxRate);
            this.totalSales = new SimpleDoubleProperty(totalSales);
            this.taxAmount = new SimpleDoubleProperty(taxAmount);
        }

        public String getTaxRate() { return taxRate.get(); }
        public double getTotalSales() { return totalSales.get(); }
        public double getTaxAmount() { return taxAmount.get(); }
    }

    // Main method to create the reports view
    public Node getView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f7;");
        mainLayout.setPadding(new Insets(20));

        // Create the header section
        VBox headerSection = createHeaderSection();
        mainLayout.setTop(headerSection);

        // Create a TabPane for different report types
        TabPane reportTabs = new TabPane();
        reportTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        reportTabs.setStyle("-fx-background-color: transparent;");

        // Sales Summary Tab
        Tab salesTab = new Tab("Sales Summary");
        salesTab.setContent(createSalesSummaryTab());
        
        // Product Performance Tab
        Tab productsTab = new Tab("Product Performance");
        productsTab.setContent(createProductPerformanceTab());
        
        // Tax Reports Tab
        Tab taxTab = new Tab("Tax Reports");
        taxTab.setContent(createTaxReportsTab());
        
        // Financial Reports Tab
        Tab financialTab = new Tab("Financial Reports");
        financialTab.setContent(createFinancialReportsTab());

        reportTabs.getTabs().addAll(salesTab, productsTab, taxTab, financialTab);
        mainLayout.setCenter(reportTabs);
        
        return mainLayout;
    }

    // Header section with filters and export controls
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(15);
        headerSection.setPadding(new Insets(0, 0, 20, 0));
        
        // Title
        Label reportTitle = new Label("Business Reports & Analytics");
        reportTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        // Date filter section
        HBox dateFilterBox = new HBox(15);
        dateFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dateRangeLabel = new Label("Date Range:");
        dateRangeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ComboBox<String> presetDates = new ComboBox<>();
        presetDates.getItems().addAll("Today", "Yesterday", "Last 7 Days", "This Month", "Last Month", "Custom Range");
        presetDates.setValue("This Month");
        presetDates.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        Label fromLabel = new Label("From:");
        fromLabel.setStyle("-fx-font-size: 14px;");
        
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        fromDate.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        Label toLabel = new Label("To:");
        toLabel.setStyle("-fx-font-size: 14px;");
        
        DatePicker toDate = new DatePicker(LocalDate.now());
        toDate.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        Button applyFilterButton = new Button("Apply Filter");
        applyFilterButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-background-radius: 5px;");
        
        dateFilterBox.getChildren().addAll(dateRangeLabel, presetDates, fromLabel, fromDate, toLabel, toDate, applyFilterButton);
        
        // Export options
        HBox exportOptionsBox = new HBox(10);
        exportOptionsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportPdfButton = new Button("Export PDF");
        exportPdfButton.setStyle("-fx-background-color: white; -fx-text-fill: #c62828; " +
                             "-fx-border-color: #c62828; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        Button exportExcelButton = new Button("Export Excel");
        exportExcelButton.setStyle("-fx-background-color: white; -fx-text-fill: #2e7d32; " +
                              "-fx-border-color: #2e7d32; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        Button printReportButton = new Button("Print Report");
        printReportButton.setStyle("-fx-background-color: white; -fx-text-fill: #0277bd; " +
                               "-fx-border-color: #0277bd; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        exportOptionsBox.getChildren().addAll(spacer, exportPdfButton, exportExcelButton, printReportButton);
        
        HBox headerControls = new HBox();
        headerControls.getChildren().addAll(dateFilterBox);
        HBox.setHgrow(dateFilterBox, Priority.ALWAYS);
        
        headerSection.getChildren().addAll(reportTitle, headerControls, exportOptionsBox);
        
        return headerSection;
    }

    // Sales Summary Tab
    private Node createSalesSummaryTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 0, 0, 0));
        
        // Summary Cards
        HBox summaryCards = createSummaryCards();
        
        // Sales Chart
        VBox chartSection = new VBox(10);
        Label chartTitle = new Label("Sales Trend");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create line chart for sales trend
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (MWK)");
        
        LineChart<String, Number> salesChart = new LineChart<>(xAxis, yAxis);
        salesChart.setTitle("Daily Sales (Last 30 Days)");
        salesChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        salesChart.setPrefHeight(400);
        salesChart.setAnimated(false);
        salesChart.setLegendVisible(true);
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        
        // Add sample data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate date = LocalDate.now().minusDays(30);
        
        for (int i = 0; i < 30; i++) {
            // Generate some randomized data for the demonstration
            double revenue = 50000 + Math.random() * 30000;
            revenueSeries.getData().add(new XYChart.Data<>(date.format(formatter), revenue));
            date = date.plusDays(1);
        }
        
        salesChart.getData().add(revenueSeries);
        chartSection.getChildren().addAll(chartTitle, salesChart);
        
        // Sales Summary Table
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("Daily Sales Summary");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TableView<SaleSummary> salesTable = new TableView<>();
        salesTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        TableColumn<SaleSummary, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<SaleSummary, Integer> transactionCol = new TableColumn<>("Transactions");
        transactionCol.setCellValueFactory(new PropertyValueFactory<>("transactions"));
        
        TableColumn<SaleSummary, Double> revenueCol = new TableColumn<>("Revenue (MWK)");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setCellFactory(col -> new javafx.scene.control.TableCell<SaleSummary, Double>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", revenue));
                }
            }
        });
        
        TableColumn<SaleSummary, Double> taxCol = new TableColumn<>("Tax (MWK)");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));
        taxCol.setCellFactory(col -> new javafx.scene.control.TableCell<SaleSummary, Double>() {
            @Override
            protected void updateItem(Double tax, boolean empty) {
                super.updateItem(tax, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", tax));
                }
            }
        });
        
        salesTable.getColumns().addAll(dateCol, transactionCol, revenueCol, taxCol);
        
        // Create mock data
        ObservableList<SaleSummary> salesData = FXCollections.observableArrayList(
            new SaleSummary("2025-05-01", 45, 126500.00, 16445.00),
            new SaleSummary("2025-04-30", 38, 98750.00, 12837.50),
            new SaleSummary("2025-04-29", 42, 113200.00, 14716.00),
            new SaleSummary("2025-04-28", 35, 87300.00, 11349.00),
            new SaleSummary("2025-04-27", 28, 76540.00, 9950.20),
            new SaleSummary("2025-04-26", 52, 142300.00, 18499.00),
            new SaleSummary("2025-04-25", 47, 135750.00, 17647.50)
        );
        
        salesTable.setItems(salesData);
        tableSection.getChildren().addAll(tableTitle, salesTable);
        
        content.getChildren().addAll(summaryCards, chartSection, tableSection);
        
        return content;
    }

    // Create summary cards for dashboard
    private HBox createSummaryCards() {
        HBox summaryCards = new HBox(15);
        summaryCards.setAlignment(Pos.CENTER);
        
        // Today Sales Card
        VBox todaySalesCard = createSummaryCard("Today's Sales", "MWK 126,500.00", "+15% from yesterday", "#1a237e");
        
        // Monthly Sales Card
        VBox monthlySalesCard = createSummaryCard("Monthly Sales", "MWK 2,845,320.00", "+8% from last month", "#00796b");
        
        // Transactions Card
        VBox transactionsCard = createSummaryCard("Transactions", "287", "45 today", "#0277bd");
        
        // Total Tax Card
        VBox taxCard = createSummaryCard("Total Tax", "MWK 369,891.60", "16.5% VAT rate", "#c62828");
        
        summaryCards.getChildren().addAll(todaySalesCard, monthlySalesCard, transactionsCard, taxCard);
        HBox.setHgrow(todaySalesCard, Priority.ALWAYS);
        HBox.setHgrow(monthlySalesCard, Priority.ALWAYS);
        HBox.setHgrow(transactionsCard, Priority.ALWAYS);
        HBox.setHgrow(taxCard, Priority.ALWAYS);
        
        return summaryCards;
    }

    // Create individual summary card
    private VBox createSummaryCard(String title, String value, String subtext, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label subtextLabel = new Label(subtext);
        subtextLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        
        // Add colored indicator
        Rectangle indicator = new Rectangle(50, 4);
        indicator.setFill(Color.web(color));
        indicator.setArcWidth(2);
        indicator.setArcHeight(2);
        
        card.getChildren().addAll(titleLabel, valueLabel, subtextLabel, indicator);
        
        return card;
    }

    // Product Performance Tab
    private Node createProductPerformanceTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 0, 0, 0));
        
        // Top Products section
        HBox chartSection = new HBox(20);
        chartSection.setPrefHeight(350);
        
        // Product Sales Chart (Bar Chart)
        VBox barChartBox = new VBox(10);
        barChartBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(barChartBox, Priority.ALWAYS);
        
        Label barChartTitle = new Label("Top 10 Products by Sales");
        barChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Sales (MWK)");
        
        BarChart<String, Number> productBarChart = new BarChart<>(xAxis, yAxis);
        productBarChart.setTitle("Top Selling Products");
        productBarChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        productBarChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Sugar", 345600));
        series.getData().add(new XYChart.Data<>("Flour", 289750));
        series.getData().add(new XYChart.Data<>("Cooking Oil", 276300));
        series.getData().add(new XYChart.Data<>("Rice", 243800));
        series.getData().add(new XYChart.Data<>("Milk", 196750));
        series.getData().add(new XYChart.Data<>("Bread", 154300));
        series.getData().add(new XYChart.Data<>("Soap", 132800));
        series.getData().add(new XYChart.Data<>("Salt", 98500));
        series.getData().add(new XYChart.Data<>("Maize", 85600));
        series.getData().add(new XYChart.Data<>("Tea", 76300));
        
        productBarChart.getData().add(series);
        barChartBox.getChildren().addAll(barChartTitle, productBarChart);
        
        // Product Category Pie Chart
        VBox pieChartBox = new VBox(10);
        pieChartBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(pieChartBox, Priority.ALWAYS);
        
        Label pieChartTitle = new Label("Sales by Category");
        pieChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        PieChart categoryPieChart = new PieChart();
        categoryPieChart.setTitle("Category Distribution");
        categoryPieChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        categoryPieChart.setLabelsVisible(true);
        
        PieChart.Data slice1 = new PieChart.Data("Food & Beverages", 45);
        PieChart.Data slice2 = new PieChart.Data("Household Items", 23);
        PieChart.Data slice3 = new PieChart.Data("Personal Care", 12);
        PieChart.Data slice4 = new PieChart.Data("Stationery", 8);
        PieChart.Data slice5 = new PieChart.Data("Electronics", 7);
        PieChart.Data slice6 = new PieChart.Data("Others", 5);
        
        categoryPieChart.getData().addAll(slice1, slice2, slice3, slice4, slice5, slice6);
        pieChartBox.getChildren().addAll(pieChartTitle, categoryPieChart);
        
        chartSection.getChildren().addAll(barChartBox, pieChartBox);
        
        // Product Sales Table
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("Product Sales Analysis");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TableView<ProductSale> productTable = new TableView<>();
        productTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        TableColumn<ProductSale, String> productNameCol = new TableColumn<>("Product Name");
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameCol.setPrefWidth(250);
        
        TableColumn<ProductSale, Integer> quantityCol = new TableColumn<>("Quantity Sold");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<ProductSale, Double> revenueCol = new TableColumn<>("Revenue (MWK)");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setCellFactory(col -> new javafx.scene.control.TableCell<ProductSale, Double>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", revenue));
                }
            }
        });
        
        TableColumn<ProductSale, Double> profitCol = new TableColumn<>("Profit (MWK)");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        profitCol.setCellFactory(col -> new javafx.scene.control.TableCell<ProductSale, Double>() {
            @Override
            protected void updateItem(Double profit, boolean empty) {
                super.updateItem(profit, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", profit));
                }
            }
        });
        
        productTable.getColumns().addAll(productNameCol, quantityCol, revenueCol, profitCol);
        
        // Create mock data
        ObservableList<ProductSale> productData = FXCollections.observableArrayList(
            new ProductSale("Sugar (1kg)", 1728, 345600.00, 86400.00),
            new ProductSale("Flour (2kg)", 1159, 289750.00, 72437.50),
            new ProductSale("Cooking Oil (2L)", 921, 276300.00, 69075.00),
            new ProductSale("Rice (5kg)", 488, 243800.00, 60950.00),
            new ProductSale("Milk (1L)", 1311, 196750.00, 49187.50),
            new ProductSale("Bread (400g)", 3086, 154300.00, 38575.00),
            new ProductSale("Soap (250g)", 1328, 132800.00, 33200.00),
            new ProductSale("Salt (500g)", 1970, 98500.00, 24625.00),
            new ProductSale("Maize Flour (10kg)", 214, 85600.00, 21400.00),
            new ProductSale("Tea (100g)", 763, 76300.00, 19075.00)
        );
        
        productTable.setItems(productData);
        tableSection.getChildren().addAll(tableTitle, productTable);
        
        content.getChildren().addAll(chartSection, tableSection);
        
        return content;
    }

    // Tax Reports Tab
    private Node createTaxReportsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 0, 0, 0));
        
        // Tax Summary Cards
        HBox taxSummaryCards = new HBox(15);
        
        VBox totalTaxCard = createSummaryCard("Total VAT Collected", "MWK 369,891.60", "Current Month", "#1a237e");
        VBox standardRateCard = createSummaryCard("Standard Rate (16.5%)", "MWK 306,350.40", "83% of total tax", "#00796b");
        VBox zeroRatedCard = createSummaryCard("Zero-Rated (0%)", "MWK 0.00", "7% of transactions", "#0277bd");
        VBox exemptCard = createSummaryCard("Exempt Sales", "MWK 421,560.00", "15% of total sales", "#c62828");
        
        taxSummaryCards.getChildren().addAll(totalTaxCard, standardRateCard, zeroRatedCard, exemptCard);
        HBox.setHgrow(totalTaxCard, Priority.ALWAYS);
        HBox.setHgrow(standardRateCard, Priority.ALWAYS);
        HBox.setHgrow(zeroRatedCard, Priority.ALWAYS);
        HBox.setHgrow(exemptCard, Priority.ALWAYS);
        
        // Tax Breakdown Chart
        VBox taxChartSection = new VBox(10);
        Label taxChartTitle = new Label("Tax Collection Trend");
        taxChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create bar chart for tax trend
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Tax Amount (MWK)");
        
        BarChart<String, Number> taxChart = new BarChart<>(xAxis, yAxis);
        taxChart.setTitle("Daily Tax Collection (Last 30 Days)");
        taxChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        taxChart.setAnimated(false);
        
        XYChart.Series<String, Number> taxSeries = new XYChart.Series<>();
        taxSeries.setName("VAT Collected");
        
        // Add sample data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        LocalDate date = LocalDate.now().minusDays(30);
        
        for (int i = 0; i < 30; i++) {
            // Generate some randomized data for the demonstration
            double taxAmount = 8000 + Math.random() * 5000;
            taxSeries.getData().add(new XYChart.Data<>(date.format(formatter), taxAmount));
            date = date.plusDays(1);
        }
        
        taxChart.getData().add(taxSeries);
        taxChartSection.getChildren().addAll(taxChartTitle, taxChart);
        taxChart.setPrefHeight(350);
        
        // Tax Summary Table
        VBox taxTableSection = new VBox(10);
        Label taxTableTitle = new Label("Tax Rate Breakdown");
        taxTableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        TableView<TaxSummary> taxTable = new TableView<>();
        taxTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        TableColumn<TaxSummary, String> taxRateCol = new TableColumn<>("Tax Rate");
        taxRateCol.setCellValueFactory(new PropertyValueFactory<>("taxRate"));
        taxRateCol.setPrefWidth(200);
        
        TableColumn<TaxSummary, Double> totalSalesCol = new TableColumn<>("Total Sales (MWK)");
        totalSalesCol.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        totalSalesCol.setCellFactory(col -> new javafx.scene.control.TableCell<TaxSummary, Double>() {
            @Override
            protected void updateItem(Double sales, boolean empty) {
                super.updateItem(sales, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", sales));
                }
            }
        });
        
        TableColumn<TaxSummary, Double> taxAmountCol = new TableColumn<>("Tax Amount (MWK)");
        taxAmountCol.setCellValueFactory(new PropertyValueFactory<>("taxAmount"));
        taxAmountCol.setCellFactory(col -> new javafx.scene.control.TableCell<TaxSummary, Double>() {
            @Override
            protected void updateItem(Double tax, boolean empty) {
                super.updateItem(tax, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", tax));
                }
            }
        });
        
        taxTable.getColumns().addAll(taxRateCol, totalSalesCol, taxAmountCol);
        
        // Create mock data
        ObservableList<TaxSummary> taxData = FXCollections.observableArrayList(
            new TaxSummary("Standard Rate (16.5%)", 1856700.00, 306350.40),
            new TaxSummary("Zero Rate (0%)", 198340.00, 0.00),
            new TaxSummary("Exempt", 421560.00, 0.00),
            new TaxSummary("Reduced Rate (10%)", 635412.00, 63541.20)
        );
        
        taxTable.setItems(taxData);
        taxTableSection.getChildren().addAll(taxTableTitle, taxTable);
        
        content.getChildren().addAll(taxSummaryCards, taxChartSection, taxTableSection);
        
        return content;
    }

    // Financial Reports Tab
private Node createFinancialReportsTab() {
    VBox content = new VBox(20);
    content.setPadding(new Insets(20, 0, 0, 0));
    
    // Financial Summary Cards
    HBox financialSummaryCards = new HBox(15);
    
    VBox revenueCard = createSummaryCard("Total Revenue", "MWK 2,845,320.00", "Current Month", "#1a237e");
    VBox costCard = createSummaryCard("Cost of Goods", "MWK 1,991,724.00", "70% of revenue", "#00796b");
    VBox profitCard = createSummaryCard("Gross Profit", "MWK 853,596.00", "30% margin", "#0277bd");
    VBox expenseCard = createSummaryCard("Operating Expenses", "MWK 341,438.40", "12% of revenue", "#c62828");
    
    financialSummaryCards.getChildren().addAll(revenueCard, costCard, profitCard, expenseCard);
    HBox.setHgrow(revenueCard, Priority.ALWAYS);
    HBox.setHgrow(costCard, Priority.ALWAYS);
    HBox.setHgrow(profitCard, Priority.ALWAYS);
    HBox.setHgrow(expenseCard, Priority.ALWAYS);
    
    // Financial Charts Section
    HBox chartSection = new HBox(20);
    chartSection.setPrefHeight(350);
    
    // Revenue vs Profit Chart
    VBox profitChartBox = new VBox(10);
    profitChartBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
    HBox.setHgrow(profitChartBox, Priority.ALWAYS);
    
    Label profitChartTitle = new Label("Revenue vs Profit");
    profitChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Month");
    yAxis.setLabel("Amount (MWK)");
    
    BarChart<String, Number> financialBarChart = new BarChart<>(xAxis, yAxis);
    financialBarChart.setTitle("Monthly Performance");
    financialBarChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
    
    XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
    revenueSeries.setName("Revenue");
    revenueSeries.getData().add(new XYChart.Data<>("Jan", 2345600));
    revenueSeries.getData().add(new XYChart.Data<>("Feb", 2189750));
    revenueSeries.getData().add(new XYChart.Data<>("Mar", 2576300));
    revenueSeries.getData().add(new XYChart.Data<>("Apr", 2843800));
    revenueSeries.getData().add(new XYChart.Data<>("May", 2845320));
    
    XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
    profitSeries.setName("Profit");
    profitSeries.getData().add(new XYChart.Data<>("Jan", 703680));
    profitSeries.getData().add(new XYChart.Data<>("Feb", 656925));
    profitSeries.getData().add(new XYChart.Data<>("Mar", 772890));
    profitSeries.getData().add(new XYChart.Data<>("Apr", 853140));
    profitSeries.getData().add(new XYChart.Data<>("May", 853596));
    
    financialBarChart.getData().addAll(revenueSeries, profitSeries);
    profitChartBox.getChildren().addAll(profitChartTitle, financialBarChart);
    
    // Expense Breakdown Pie Chart
    VBox expenseChartBox = new VBox(10);
    expenseChartBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
    HBox.setHgrow(expenseChartBox, Priority.ALWAYS);
    
    Label expenseChartTitle = new Label("Expense Breakdown");
    expenseChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    
    PieChart expensePieChart = new PieChart();
    expensePieChart.setTitle("Expense Distribution");
    expensePieChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
    expensePieChart.setLabelsVisible(true);
    
    PieChart.Data slice1 = new PieChart.Data("Salaries", 45);
    PieChart.Data slice2 = new PieChart.Data("Rent", 20);
    PieChart.Data slice3 = new PieChart.Data("Utilities", 12);
    PieChart.Data slice4 = new PieChart.Data("Transport", 8);
    PieChart.Data slice5 = new PieChart.Data("Marketing", 10);
    PieChart.Data slice6 = new PieChart.Data("Others", 5);
    
    expensePieChart.getData().addAll(slice1, slice2, slice3, slice4, slice5, slice6);
    expenseChartBox.getChildren().addAll(expenseChartTitle, expensePieChart);
    
    chartSection.getChildren().addAll(profitChartBox, expenseChartBox);
    
    // Financial Summary Table
    VBox tableSection = new VBox(10);
    Label tableTitle = new Label("Profit & Loss Statement");
    tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    
    GridPane financialTable = new GridPane();
    financialTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px; -fx-padding: 15;");
    financialTable.setHgap(10);
    financialTable.setVgap(10);
    
    // Headers
    Label headerCategory = new Label("Category");
    headerCategory.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label headerAmount = new Label("Amount (MWK)");
    headerAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label headerPercentage = new Label("% of Revenue");
    headerPercentage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    
    financialTable.add(headerCategory, 0, 0);
    financialTable.add(headerAmount, 1, 0);
    financialTable.add(headerPercentage, 2, 0);
    
    // Revenue section
    Label revenueLabel = new Label("Revenue");
    revenueLabel.setStyle("-fx-font-weight: bold;");
    Label revenueValue = new Label("2,845,320.00");
    Label revenuePercentage = new Label("100%");
    
    financialTable.add(revenueLabel, 0, 1);
    financialTable.add(revenueValue, 1, 1);
    financialTable.add(revenuePercentage, 2, 1);
    
    // Cost of Goods section
    Label costLabel = new Label("Cost of Goods Sold");
    Label costValue = new Label("1,991,724.00");
    Label costPercentage = new Label("70%");
    
    financialTable.add(costLabel, 0, 2);
    financialTable.add(costValue, 1, 2);
    financialTable.add(costPercentage, 2, 2);
    
    // Gross Profit section
    Label grossProfitLabel = new Label("Gross Profit");
    grossProfitLabel.setStyle("-fx-font-weight: bold;");
    Label grossProfitValue = new Label("853,596.00");
    grossProfitValue.setStyle("-fx-font-weight: bold;");
    Label grossProfitPercentage = new Label("30%");
    grossProfitPercentage.setStyle("-fx-font-weight: bold;");
    
    financialTable.add(grossProfitLabel, 0, 3);
    financialTable.add(grossProfitValue, 1, 3);
    financialTable.add(grossProfitPercentage, 2, 3);
    
    // Expenses section header
    Label expensesHeaderLabel = new Label("Operating Expenses");
    expensesHeaderLabel.setStyle("-fx-font-weight: bold;");
    
    financialTable.add(expensesHeaderLabel, 0, 4);
    
    // Expense categories
    String[] expenseCategories = {"Salaries", "Rent", "Utilities", "Transport", "Marketing", "Other Expenses"};
    double[] expenseValues = {153648.28, 68287.68, 40972.61, 27315.07, 34143.84, 17071.92};
    double[] expensePercentages = {5.4, 2.4, 1.44, 0.96, 1.2, 0.6};
    
    for (int i = 0; i < expenseCategories.length; i++) {
        Label categoryLabel = new Label(expenseCategories[i]);
        Label valueLabel = new Label(String.format("%,.2f", expenseValues[i]));
        Label percentageLabel = new Label(String.format("%.1f%%", expensePercentages[i]));
        
        financialTable.add(categoryLabel, 0, 5 + i);
        financialTable.add(valueLabel, 1, 5 + i);
        financialTable.add(percentageLabel, 2, 5 + i);
    }
    
    // Total Expenses
    Label totalExpensesLabel = new Label("Total Operating Expenses");
    totalExpensesLabel.setStyle("-fx-font-weight: bold;");
    Label totalExpensesValue = new Label("341,438.40");
    totalExpensesValue.setStyle("-fx-font-weight: bold;");
    Label totalExpensesPercentage = new Label("12%");
    totalExpensesPercentage.setStyle("-fx-font-weight: bold;");
    
    financialTable.add(totalExpensesLabel, 0, 11);
    financialTable.add(totalExpensesValue, 1, 11);
    financialTable.add(totalExpensesPercentage, 2, 11);
    
    // Net Profit
    Label netProfitLabel = new Label("Net Profit");
    netProfitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    Label netProfitValue = new Label("512,157.60");
    netProfitValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #00796b;");
    Label netProfitPercentage = new Label("18%");
    netProfitPercentage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    
    financialTable.add(netProfitLabel, 0, 12);
    financialTable.add(netProfitValue, 1, 12);
    financialTable.add(netProfitPercentage, 2, 12);
    
    tableSection.getChildren().addAll(tableTitle, financialTable);
    
    content.getChildren().addAll(financialSummaryCards, chartSection, tableSection);
    
    return content;
}

}
