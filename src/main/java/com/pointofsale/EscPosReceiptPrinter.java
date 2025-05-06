package com.pointofsale;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.helper.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Premium ESC/POS receipt printer implementation
 * Designed for professional, high-quality receipts with perfect formatting,
 * consistent alignment, and reliable QR code printing for validation.
 * Includes legal receipt demarcation for compliance and customer confidence.
 */
public class EscPosReceiptPrinter {

    // ESC/POS Command Constants
    private static final byte[] ESC_INIT = {0x1B, 0x40};           // Initialize printer
    private static final byte[] LF = {0x0A};                       // Line Feed
    private static final byte[] CR = {0x0D};                       // Carriage Return
    private static final byte[] ESC_ALIGN_LEFT = {0x1B, 0x61, 0x00};  // Left alignment
    private static final byte[] ESC_ALIGN_CENTER = {0x1B, 0x61, 0x01}; // Center alignment
    private static final byte[] ESC_ALIGN_RIGHT = {0x1B, 0x61, 0x02};  // Right alignment
    private static final byte[] ESC_EMPHASIZE_ON = {0x1B, 0x45, 0x01}; // Bold ON
    private static final byte[] ESC_EMPHASIZE_OFF = {0x1B, 0x45, 0x00}; // Bold OFF
    private static final byte[] ESC_DOUBLE_WIDTH_ON = {0x1B, 0x21, 0x10}; // Double width ON
    private static final byte[] ESC_DOUBLE_WIDTH_OFF = {0x1B, 0x21, 0x00}; // Double width OFF
    private static final byte[] ESC_DOUBLE_HEIGHT_ON = {0x1B, 0x21, 0x10}; // Double height ON
    private static final byte[] ESC_DOUBLE_HEIGHT_OFF = {0x1B, 0x21, 0x00}; // Double height OFF
    private static final byte[] ESC_UNDERLINE_ON = {0x1B, 0x2D, 0x01};     // Underline ON
    private static final byte[] ESC_UNDERLINE_OFF = {0x1B, 0x2D, 0x00};    // Underline OFF
    private static final byte[] GS_CUT_PAPER = {0x1D, 0x56, 0x41, 0x10}; // Cut paper with feed
    private static final byte[] ESC_DRAWER_KICK = {0x1B, 0x70, 0x00, 0x32, (byte) 0xFA}; // Open cash drawer
    
    // Custom printer constants
    private static final String CHARSET = "CP437"; // Standard ESC/POS character set
    private static final int RECEIPT_WIDTH = 40; // Character width of receipt
    private static final int LEFT_MARGIN = 0; // Left margin for consistent alignment
    
    /**
     * Prints a premium receipt with QR code for validation
     */
    public static void printReceipt(
            InvoiceHeader invoiceHeader,
            String buyersName,
            List<LineItemDto> lineItems,
            String validationUrl,
            double amountTendered,
            double change,
            List<TaxBreakDown> invoiceTaxBreakDown
    ) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String tin = Helper.getTin();
        String companyName = Helper.getTrading();
        String storeName = Helper.getStoreName();
        String storeAddress = Helper.getStoreAddress();
        String storePhone = Helper.getStorePhone();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = LocalDateTime.now().format(formatter);

        try {
            // Initialize printer
            output.write(ESC_INIT);
            output.write(LF);
            
            // Set center alignment for all content
            output.write(ESC_ALIGN_CENTER);
            
            // Company Header Block
            output.write(ESC_EMPHASIZE_ON);
            output.write(ESC_DOUBLE_WIDTH_ON);
            output.write(companyName.getBytes(CHARSET));
            output.write(ESC_DOUBLE_WIDTH_OFF);
            output.write(LF);
            output.write(ESC_EMPHASIZE_OFF);
            
            // Store information
            output.write(storeName.getBytes(CHARSET));
            output.write(LF);
            output.write(storeAddress.getBytes(CHARSET));
            output.write(LF);
            output.write(("Tel: " + storePhone).getBytes(CHARSET));
            output.write(LF);
            output.write(("TIN: " + tin).getBytes(CHARSET));
            output.write(LF);
            output.write(LF);
            
            // Legal Receipt Start Marker
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** START OF LEGAL RECEIPT ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            output.write(LF);
            
            // Receipt Title
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** TAX INVOICE ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            
            // Pretty border
            printStarDivider(output);
            
            // Transaction Details - professionally formatted to match divider width
            int fullWidth = RECEIPT_WIDTH;
            int labelWidth = 10;
            int valueWidth = fullWidth - labelWidth - 3; // 3 for " : "
            
            String receiptLabel = "Receipt#";
            String receiptValue = invoiceHeader.getInvoiceNumber();
            String receiptLine = String.format("%-" + labelWidth + "s : %-" + valueWidth + "s", receiptLabel, receiptValue);
            output.write(receiptLine.getBytes(CHARSET));
            output.write(LF);
            
            String dateLabel = "Date";
            String dateValue = currentDateTime;
            String dateLine = String.format("%-" + labelWidth + "s : %-" + valueWidth + "s", dateLabel, dateValue);
            output.write(dateLine.getBytes(CHARSET));
            output.write(LF);
            
            String buyerLabel = "Buyer";
            String buyerValue = buyersName;
            String buyerLine = String.format("%-" + labelWidth + "s : %-" + valueWidth + "s", buyerLabel, buyerValue);
            output.write(buyerLine.getBytes(CHARSET));
            output.write(LF);
            
            // Divider before items
            printSolidDivider(output);
            
            // Table header for items
            StringBuilder header = new StringBuilder();
            header.append(String.format("%-15s %-8s %5s %8s", "Item", "Unit", "Qty", "Amount"));
            output.write(header.toString().getBytes(CHARSET));
            output.write(LF);
            
            // Divider again
            printSolidDivider(output);
            
            // Line items with proper formatting
            for (LineItemDto item : lineItems) {
                // Item description - may need to wrap for long descriptions
                String description = item.getDescription();
                if (description.length() > 15) {
                    // Truncate or implement word wrapping as needed
                    description = description.substring(0, 15);
                }
                
                StringBuilder itemLine = new StringBuilder();
                itemLine.append(String.format("%-15s ", description));
                itemLine.append(String.format("%-8s ", formatCurrency(item.getUnitPrice())));
                itemLine.append(String.format("%5s ", item.getQuantity()));
                itemLine.append(String.format("%8s", formatCurrency(item.getQuantity() * item.getUnitPrice())));
                
                output.write(itemLine.toString().getBytes(CHARSET));
                output.write(LF);
            }
            
            // Divider before totals
            printSolidDivider(output);
            
            // Calculate totals
            double totalVAT = invoiceTaxBreakDown.stream().mapToDouble(TaxBreakDown::getTaxAmount).sum();
            double subtotal = invoiceTaxBreakDown.stream().mapToDouble(TaxBreakDown::getTaxableAmount).sum();
            double invoiceTotal = subtotal + totalVAT;
            
            // Print subtotal with professional formatting - full width matching dividers
            int totalLabelWidth = 12;
            int totalValueWidth = fullWidth - totalLabelWidth - 3; // 3 for " : "
            
            String subtotalLabel = "Subtotal";
            String subtotalValue = formatCurrency(subtotal);
            String subtotalLine = String.format("%-" + totalLabelWidth + "s : %-" + totalValueWidth + "s", subtotalLabel, subtotalValue);
            output.write(subtotalLine.getBytes(CHARSET));
            output.write(LF);
            
            // Tax breakdowns with professional formatting - full width matching dividers
            for (TaxBreakDown tax : invoiceTaxBreakDown) {
                String taxLabel = "VAT " + tax.getRateId() + "%";
                String taxValue = formatCurrency(tax.getTaxAmount());
                String taxLine = String.format("%-" + totalLabelWidth + "s : %-" + totalValueWidth + "s", taxLabel, taxValue);
                output.write(taxLine.getBytes(CHARSET));
                output.write(LF);
            }
            
            // Total, emphasized with professional formatting - full width matching dividers
            output.write(ESC_EMPHASIZE_ON);
            String totalLabel = "TOTAL";
            String totalValue = formatCurrency(invoiceTotal);
            String totalLine = String.format("%-" + totalLabelWidth + "s : %-" + totalValueWidth + "s", totalLabel, totalValue);
            output.write(totalLine.getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            
            // Payment information with professional formatting - full width matching dividers
            String paidLabel = "Amount Paid";
            String paidValue = formatCurrency(amountTendered);
            String paidLine = String.format("%-" + totalLabelWidth + "s : %-" + totalValueWidth + "s", paidLabel, paidValue);
            output.write(paidLine.getBytes(CHARSET));
            output.write(LF);
            
            if (change > 0) {
                String changeLabel = "Change";
                String changeValue = formatCurrency(change);
                String changeLine = String.format("%-" + totalLabelWidth + "s : %-" + totalValueWidth + "s", changeLabel, changeValue);
                output.write(changeLine.getBytes(CHARSET));
                output.write(LF);
            }
            
            output.write(LF);
            
            // Validation section
            printStarDivider(output);
            
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** VALIDATE YOUR RECEIPT ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            
            // QR Code - using ESC/POS native commands
            printQRCode(output, validationUrl);
            
            // Footer
            output.write(LF);
            printStarDivider(output);
            
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** THANK YOU FOR YOUR BUSINESS ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            output.write("Keep this receipt for your records".getBytes(CHARSET));
            output.write(LF);
            output.write(LF);
            
            // Legal Receipt End Marker
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** END OF LEGAL RECEIPT ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            output.write(LF);
            output.write(LF);
            
            // Drawer kick and cut paper
            output.write(ESC_DRAWER_KICK);
            output.write(GS_CUT_PAPER);
            
            // Send to printer
            sendToPrinter(output.toByteArray());
            
        } catch (Exception e) {
            throw new Exception("Failed to print receipt: " + e.getMessage(), e);
        }
    }
    
    /**
     * Print a star-based divider line - keeps the existing alignment setting
     */
    private static void printStarDivider(ByteArrayOutputStream output) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RECEIPT_WIDTH; i++) {
            sb.append("*");
        }
        output.write(sb.toString().getBytes(CHARSET));
        output.write(LF);
    }
    
    /**
     * Print a solid divider line - keeps the existing alignment setting
     */
    private static void printSolidDivider(ByteArrayOutputStream output) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RECEIPT_WIDTH; i++) {
            sb.append("-");
        }
        output.write(sb.toString().getBytes(CHARSET));
        output.write(LF);
    }
    
    /**
     * Format a key-value pair with proper left alignment
     * Note: This method is no longer used as we're centering everything
     */
    private static String formatLeftPaddedValue(String key, String value, int leftPadding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPadding; i++) {
            sb.append(" ");
        }
        sb.append(String.format("%-10s: %s", key, value));
        return sb.toString();
    }
    
    /**
     * Format a total line with right-aligned value
     * Note: This method is no longer used as we're centering everything
     */
    private static String formatTotalLine(String label, String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LEFT_MARGIN; i++) {
            sb.append(" ");
        }
        
        int contentWidth = RECEIPT_WIDTH - LEFT_MARGIN;
        int spaces = contentWidth - label.length() - value.length();
        
        sb.append(label);
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        sb.append(value);
        return sb.toString();
    }
    
    /**
     * Format currency value without currency symbol (added in formatValue method)
     */
    private static String formatCurrency(double value) {
        return String.format("%,.2f", value);
    }

    /**
     * Prints QR code using native ESC/POS commands with optimized size and error correction
     */
    private static void printQRCode(ByteArrayOutputStream output, String data) throws IOException {
        try {
            // Make sure data isn't too long for QR code
            String qrData = data;
            if (qrData.length() > 300) {
                qrData = qrData.substring(0, 300);
            }
            
            // Center the QR code
            output.write(ESC_ALIGN_CENTER);
            
            // Clear QR code buffer
            output.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x00});
            
            // Set QR code model
            output.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00});
            
            // Set QR code size (1-16), bigger numbers = larger QR code
            byte qrSize = 4; // Larger size for better scanning
            output.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, qrSize});
            
            // Set error correction level (48 = L, 49 = M, 50 = Q, 51 = H)
            byte errorCorrectionLevel = 51; // H level = highest error correction
            output.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, errorCorrectionLevel});
            
            // Store QR code data
            byte[] qrBytes = qrData.getBytes(CHARSET);
            int dataLength = qrBytes.length + 3;
            int pL = dataLength % 256;
            int pH = dataLength / 256;
            
            output.write(new byte[]{0x1D, 0x28, 0x6B, (byte) pL, (byte) pH, 0x31, 0x50, 0x30});
            output.write(qrBytes);
            
            // Print QR code
            output.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
            
        } catch (Exception e) {
            // If QR code fails, write a message
            output.write(ESC_ALIGN_CENTER);
            output.write(LF);
            output.write("(QR Code Unavailable)".getBytes(CHARSET));
            output.write(LF);
        }
    }

    /**
     * Send print data to default printer
     */
    private static void sendToPrinter(byte[] data) throws PrintException {
        PrintService printService = findPrintService();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(data, flavor, null);
        
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(new Copies(1));
        
        DocPrintJob job = printService.createPrintJob();
        job.print(doc, attrs);
    }

    /**
     * Find the default receipt printer
     */
    private static PrintService findPrintService() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services.length == 0) {
            throw new RuntimeException("No printer found!");
        }
        
        // Use first available printer
        // You can improve this by implementing printer selection logic
        return services[0];
    }

    /**
     * Format monetary values with currency symbol
     */
    private static String formatValue(double value) {
        return String.format("MWK %,.2f", value);
    }
}