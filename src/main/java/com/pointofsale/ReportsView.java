package com.pointofsale;

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
import java.text.NumberFormat;
import java.util.Locale;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import java.util.Collections;
import com.pointofsale.helper.Helper;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import com.pointofsale.model.SaleSummary;
import com.pointofsale.model.TaxTrend;
import com.pointofsale.model.ProductSale;
import com.pointofsale.model.CategoryRevenue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.pointofsale.model.TaxSummary;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ReportsView {

    // Class fields to store UI components that need updating
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private ComboBox<String> presetDatesComboBox;
    
    // Charts that will be updated
    private LineChart<String, Number> salesChart;
    private TableView<SaleSummary> salesTable;
    private BarChart<String, Number> productBarChart;
    private PieChart categoryPieChart;
    private TableView<ProductSale> productTable;
    private BarChart<String, Number> taxChart;
    private TableView<TaxSummary> taxTable;
    
    // Cards that will be updated
    private VBox todaySalesCard;
    private VBox monthlySalesCard;
    private VBox transactionsCard;
    private VBox taxCard;
    private VBox totalTaxCard;
    private VBox standardRateCard;
    private VBox zeroRatedCard;
    private VBox exemptCard;

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
        
        reportTabs.getTabs().addAll(salesTab, productsTab, taxTab);
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
        
        presetDatesComboBox = new ComboBox<>();
        presetDatesComboBox.getItems().addAll("Today", "Yesterday", "Last 7 Days", "This Month", "Last Month", "Custom Range");
        presetDatesComboBox.setValue("This Month");
        presetDatesComboBox.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        // Add listener to preset dates combo box
        presetDatesComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateDatePickersBasedOnPreset(newValue);
            }
        });
        
        Label fromLabel = new Label("From:");
        fromLabel.setStyle("-fx-font-size: 14px;");
        
        fromDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1)); // First day of current month
        fromDatePicker.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        Label toLabel = new Label("To:");
        toLabel.setStyle("-fx-font-size: 14px;");
        
        toDatePicker = new DatePicker(LocalDate.now());
        toDatePicker.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        
        Button applyFilterButton = new Button("Apply Filter");
        applyFilterButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-background-radius: 5px;");
        
        // Add action handler to apply filter button
        applyFilterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                applyDateFilter();
            }
        });
        
        dateFilterBox.getChildren().addAll(dateRangeLabel, presetDatesComboBox, fromLabel, fromDatePicker, toLabel, toDatePicker, applyFilterButton);
        
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

    // Method to update date pickers based on selected preset
    private void updateDatePickersBasedOnPreset(String preset) {
        LocalDate today = LocalDate.now();
        
        switch (preset) {
            case "Today":
                fromDatePicker.setValue(today);
                toDatePicker.setValue(today);
                break;
            case "Yesterday":
                LocalDate yesterday = today.minusDays(1);
                fromDatePicker.setValue(yesterday);
                toDatePicker.setValue(yesterday);
                break;
            case "Last 7 Days":
                fromDatePicker.setValue(today.minusDays(6));
                toDatePicker.setValue(today);
                break;
            case "This Month":
                fromDatePicker.setValue(today.withDayOfMonth(1));
                toDatePicker.setValue(today);
                break;
            case "Last Month":
                LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
                LocalDate lastDayLastMonth = firstDayLastMonth.plusMonths(1).minusDays(1);
                fromDatePicker.setValue(firstDayLastMonth);
                toDatePicker.setValue(lastDayLastMonth);
                break;
            case "Custom Range":
                // Leave date pickers as they are for custom selection
                break;
        }
    }

    // Method to apply date filter to all reports
    private void applyDateFilter() {
        String fromDate = fromDatePicker.getValue().toString();
        String toDate = toDatePicker.getValue().toString();
        
        // Update all report components with new date range
        updateSalesSummary(fromDate, toDate);
        updateProductPerformance(fromDate, toDate);
        updateTaxReports(fromDate, toDate);
    }

    // Method to update Sales Summary tab with new date range
    private void updateSalesSummary(String fromDate, String toDate) {
        // Update summary cards
        updateSummaryCards(fromDate, toDate);
        
        // Update sales chart
        List<SaleSummary> summaries = Helper.getSalesSummaryByDateRange(fromDate, toDate);
        Collections.sort(summaries, (s1, s2) -> s1.getDate().compareTo(s2.getDate()));
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        
        for (SaleSummary summary : summaries) {
            revenueSeries.getData().add(new XYChart.Data<>(summary.getDate(), summary.getRevenue()));
        }
        
        salesChart.getData().clear();
        salesChart.getData().add(revenueSeries);
        
        // Update sales table
        salesTable.setItems(FXCollections.observableArrayList(summaries));
    }

    // Method to update summary cards with new date range data
    private void updateSummaryCards(String fromDate, String toDate) {
        // Get filtered data
        double periodSales = Helper.getSalesTotalByDateRange(fromDate, toDate);
        double previousPeriodSales = Helper.getSalesTotalForPreviousPeriod(fromDate, toDate);
        int transactionCount = Helper.getTransactionCountByDateRange(fromDate, toDate);
        double taxTotal = Helper.getTaxTotalByDateRange(fromDate, toDate);
        
        // Calculate changes
        String salesChange = Helper.getPercentageChange(periodSales, previousPeriodSales);
        
        // Update cards with new values
        updateSummaryCard(todaySalesCard, "Period Sales", String.format("MWK %,.2f", periodSales), 
                          salesChange + " vs previous period", "#1a237e");
        updateSummaryCard(transactionsCard, "Transactions", String.valueOf(transactionCount), 
                          "Total for selected period", "#0277bd");
        updateSummaryCard(taxCard, "Total Tax", String.format("MWK %,.2f", taxTotal), 
                          "16.5% VAT rate", "#c62828");
    }

    // Method to update a summary card with new values
    private void updateSummaryCard(VBox card, String title, String value, String subtext, String color) {
        if (card != null && card.getChildren().size() >= 3) {
            ((Label) card.getChildren().get(0)).setText(title);
            ((Label) card.getChildren().get(1)).setText(value);
            ((Label) card.getChildren().get(2)).setText(subtext);
        }
    }

    // Method to update Product Performance tab with new date range
    private void updateProductPerformance(String fromDate, String toDate) {
        // Update product bar chart
        List<ProductSale> productSales = Helper.getTop10ProductSalesByDateRange(fromDate, toDate);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ProductSale sale : productSales) {
            series.getData().add(new XYChart.Data<>(sale.getProductName(), sale.getRevenue()));
        }
        
        productBarChart.getData().clear();
        productBarChart.getData().add(series);
        
        // Update category pie chart
        List<CategoryRevenue> categoryRevenueList = Helper.getSalesByCategoryByDateRange(fromDate, toDate);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (CategoryRevenue categoryRevenue : categoryRevenueList) {
            pieChartData.add(new PieChart.Data(categoryRevenue.getCategory(), categoryRevenue.getRevenue()));
        }
        
        categoryPieChart.setData(pieChartData);
        
        // Update product table
        productTable.setItems(FXCollections.observableArrayList(productSales));
    }

    // Method to update Tax Reports tab with new date range
    private void updateTaxReports(String fromDate, String toDate) {
        // Update tax summary cards
        double totalVAT = Helper.fetchTotalVAT(fromDate, toDate);
        double standardRateVAT = Helper.fetchStandardRateVAT(fromDate, toDate);
        double zeroRatedVAT = Helper.fetchZeroRatedVAT(fromDate, toDate);
        double exemptSales = Helper.fetchExemptSales(fromDate, toDate);
        
        updateSummaryCard(totalTaxCard, "Total VAT Collected", formatCurrency(totalVAT), 
                          "Selected Period", "#1a237e");
        updateSummaryCard(standardRateCard, "Standard Rate (16.5%)", formatCurrency(standardRateVAT), 
                          calculatePercentage(standardRateVAT, totalVAT) + " of total tax", "#00796b");
        updateSummaryCard(zeroRatedCard, "Zero-Rated (0%)", formatCurrency(zeroRatedVAT), 
                          "Selected Period", "#0277bd");
        updateSummaryCard(exemptCard, "Exempt Sales", formatCurrency(exemptSales), 
                          calculatePercentage(exemptSales, totalVAT + exemptSales) + " of total sales", "#c62828");
        
        // Update tax chart
        List<TaxTrend> trends = Helper.fetchTaxTrends(fromDate, toDate);
        XYChart.Series<String, Number> taxSeries = new XYChart.Series<>();
        taxSeries.setName("VAT Collected");
        
        for (TaxTrend trend : trends) {
            taxSeries.getData().add(new XYChart.Data<>(trend.getDate(), trend.getVatAmount()));
        }
        
        taxChart.getData().clear();
        taxChart.getData().add(taxSeries);
        
        // Update tax table
        ObservableList<TaxSummary> taxData = Helper.fetchTaxBreakdownByDateRange(fromDate, toDate);
        taxTable.setItems(taxData);
    }

    // Calculate percentage as a string
    private String calculatePercentage(double part, double total) {
        if (total == 0) return "0%";
        double percentage = (part / total) * 100;
        return String.format("%.1f%%", percentage);
    }

    // Sales Summary Tab
    private Node createSalesSummaryTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 0, 0, 0));

        // Summary Cards
        HBox summaryCards = createSummaryCards();

        // Sales Chart Section
        VBox chartSection = new VBox(10);
        Label chartTitle = new Label("Sales Trend");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Line Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (MWK)");

        salesChart = new LineChart<>(xAxis, yAxis);
        salesChart.setTitle("Daily Sales");
        salesChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        salesChart.setPrefHeight(400);
        salesChart.setAnimated(false);
        salesChart.setLegendVisible(true);

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        // Initial data for current month
        List<SaleSummary> summaries = Helper.getDailySalesSummary(30);
        Collections.reverse(summaries);

        for (SaleSummary summary : summaries) {
            revenueSeries.getData().add(new XYChart.Data<>(summary.getDate(), summary.getRevenue()));
        }

        salesChart.getData().add(revenueSeries);
        chartSection.getChildren().addAll(chartTitle, salesChart);

        // Sales Summary Table Section
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("Daily Sales Summary");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        salesTable = new TableView<>();
        salesTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");

        TableColumn<SaleSummary, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<SaleSummary, Integer> transactionCol = new TableColumn<>("Transactions");
        transactionCol.setCellValueFactory(new PropertyValueFactory<>("transactions"));

        TableColumn<SaleSummary, Double> revenueCol = new TableColumn<>("Revenue (MWK)");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty || revenue == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", revenue));
                }
            }
        });

        TableColumn<SaleSummary, Double> taxCol = new TableColumn<>("Tax (MWK)");
        taxCol.setCellValueFactory(new PropertyValueFactory<>("tax"));
        taxCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double tax, boolean empty) {
                super.updateItem(tax, empty);
                if (empty || tax == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", tax));
                }
            }
        });

        salesTable.getColumns().addAll(dateCol, transactionCol, revenueCol, taxCol);
        salesTable.setItems(FXCollections.observableArrayList(summaries));
        tableSection.getChildren().addAll(tableTitle, salesTable);

        content.getChildren().addAll(summaryCards, chartSection, tableSection);
        return content;
    }

    private HBox createSummaryCards() {
        HBox summaryCards = new HBox(15);
        summaryCards.setAlignment(Pos.CENTER);

        // Fetch data
        double todaySales = Helper.getTodaySalesTotal();
        double yesterdaySales = Helper.getYesterdaySalesTotal();
        double monthlySales = Helper.getMonthSalesTotal();
        double lastMonthSales = Helper.getLastMonthSalesTotal();
        int todayTransactions = Helper.getTodayTransactionCount();
        double todayTax = Helper.getTodayTaxTotal();

        // Calculate changes
        String todayChange = Helper.getPercentageChange(todaySales, yesterdaySales);
        String monthChange = Helper.getPercentageChange(monthlySales, lastMonthSales);

        // Format values
        String todaySalesStr = String.format("MWK %,.2f", todaySales);
        String monthlySalesStr = String.format("MWK %,.2f", monthlySales);
        String taxStr = String.format("MWK %,.2f", todayTax);
        String transactionsStr = String.valueOf(todayTransactions);

        // Cards
        todaySalesCard = createSummaryCard("Today's Sales", todaySalesStr, todayChange + " from yesterday", "#1a237e");
        monthlySalesCard = createSummaryCard("Monthly Sales", monthlySalesStr, monthChange + " from last month", "#00796b");
        transactionsCard = createSummaryCard("Transactions", transactionsStr, todayTransactions + " today", "#0277bd");
        taxCard = createSummaryCard("Total Tax", taxStr, "16.5% VAT rate", "#c62828");

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

        productBarChart = new BarChart<>(xAxis, yAxis);
        productBarChart.setTitle("Top Selling Products");
        productBarChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        productBarChart.setLegendVisible(false);

        // Get product sales from the database
        List<ProductSale> productSales = Helper.getTop10ProductSales();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ProductSale sale : productSales) {
            series.getData().add(new XYChart.Data<>(sale.getProductName(), sale.getRevenue()));
        }

        productBarChart.getData().add(series);
        barChartBox.getChildren().addAll(barChartTitle, productBarChart);

        // Product Category Pie Chart
        VBox pieChartBox = new VBox(10);
        pieChartBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(pieChartBox, Priority.ALWAYS);

        Label pieChartTitle = new Label("Sales by Category");
        pieChartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        categoryPieChart = new PieChart();
        categoryPieChart.setTitle("Category Distribution");
        categoryPieChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        categoryPieChart.setLabelsVisible(true);

        // Get category sales data from the database
        List<CategoryRevenue> categoryRevenueList = Helper.getSalesByCategory();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (CategoryRevenue categoryRevenue : categoryRevenueList) {
            pieChartData.add(new PieChart.Data(categoryRevenue.getCategory(), categoryRevenue.getRevenue()));
        }

        categoryPieChart.getData().addAll(pieChartData);
        pieChartBox.getChildren().addAll(pieChartTitle, categoryPieChart);

        chartSection.getChildren().addAll(barChartBox, pieChartBox);

        // Product Sales Table
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("Product Sales Analysis");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        productTable = new TableView<>();
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

        // Create product data from database
        ObservableList<ProductSale> productData = FXCollections.observableArrayList(productSales);
        productTable.setItems(productData);
        tableSection.getChildren().addAll(tableTitle, productTable);

        content.getChildren().addAll(chartSection, tableSection);

        return content;
    }

    private Node createTaxReportsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 0, 0, 0));

        // Tax Summary Cards
        HBox taxSummaryCards = new HBox(15);

        // Fetch the necessary data from the DB for each summary card
        double totalVAT = Helper.fetchTotalVAT("2025-05-01", "2025-05-31");  // Example date range for May
        double standardRateVAT = Helper.fetchStandardRateVAT("2025-05-01", "2025-05-31");
        double zeroRatedVAT = Helper.fetchZeroRatedVAT("2025-05-01", "2025-05-31");
        double exemptSales = Helper.fetchExemptSales("2025-05-01", "2025-05-31");

        totalTaxCard = createSummaryCard("Total VAT Collected", formatCurrency(totalVAT), "Current Month", "#1a237e");
        standardRateCard = createSummaryCard("Standard Rate (16.5%)", formatCurrency(standardRateVAT), "83% of total tax", "#00796b");
        zeroRatedCard = createSummaryCard("Zero-Rated (0%)", formatCurrency(zeroRatedVAT), "7% of transactions", "#0277bd");
        exemptCard = createSummaryCard("Exempt Sales", formatCurrency(exemptSales), "15% of total sales", "#c62828");

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

        taxChart = new BarChart<>(xAxis, yAxis);
        taxChart.setTitle("Daily Tax Collection");
        taxChart.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        taxChart.setAnimated(false);

        XYChart.Series<String, Number> taxSeries = new XYChart.Series<>();
        taxSeries.setName("VAT Collected");

        // Fetch tax collection trend data
        List<TaxTrend> trends = Helper.fetchTaxTrends("2025-04-01", "2025-05-30");  // Example date range for April
        for (TaxTrend trend : trends) {
            taxSeries.getData().add(new XYChart.Data<>(trend.getDate(), trend.getVatAmount()));
        }

        taxChart.getData().add(taxSeries);
        taxChartSection.getChildren().addAll(taxChartTitle, taxChart);
        taxChart.setPrefHeight(350);

        // Tax Summary Table
        VBox taxTableSection = new VBox(10);
        Label taxTableTitle = new Label("Tax Rate Breakdown");
        taxTableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        taxTable = new TableView<>();
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

        // Fetch tax breakdown data
        ObservableList<TaxSummary> taxData = Helper.fetchTaxBreakdown();
        taxTable.setItems(taxData);
        taxTableSection.getChildren().addAll(taxTableTitle, taxTable);

        content.getChildren().addAll(taxSummaryCards, taxChartSection, taxTableSection);

        return content;
    }
    
    // Method to format a number as currency
    public static String formatCurrency(double amount) {
        // Use Malawian Kwacha (MWK) as the currency symbol
        Locale mwkLocale = new Locale("en", "MW");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(mwkLocale);
        
        // Format the given amount as currency
        return currencyFormat.format(amount);
    }
    
}