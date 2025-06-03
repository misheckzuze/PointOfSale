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

public class EscPosReceiptPrinter {

    // ESC/POS Command Constants
    private static final byte[] ESC_INIT = {0x1B, 0x40};
    private static final byte[] LF = {0x0A}; 
    private static final byte[] CR = {0x0D};
    private static final byte[] ESC_ALIGN_LEFT = {0x1B, 0x61, 0x00}; 
    private static final byte[] ESC_ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] ESC_ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    private static final byte[] ESC_EMPHASIZE_ON = {0x1B, 0x45, 0x01};
    private static final byte[] ESC_EMPHASIZE_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] ESC_DOUBLE_SIZE_ON = {0x1B, 0x21, 0x10}; 
    private static final byte[] ESC_DOUBLE_SIZE_OFF = {0x1B, 0x21, 0x00}; 
    private static final byte[] ESC_UNDERLINE_ON = {0x1B, 0x2D, 0x01};
    private static final byte[] ESC_UNDERLINE_OFF = {0x1B, 0x2D, 0x00};
    private static final byte[] GS_CUT_PAPER = {0x1D, 0x56, 0x41, 0x10};
    private static final byte[] ESC_DRAWER_KICK = {0x1B, 0x70, 0x00, 0x32, (byte) 0xFA};
    
    // Custom printer constants
    private static final String CHARSET = "CP437";
    private static final int RECEIPT_WIDTH = 40;
   
    public static void printReceipt(
            InvoiceHeader invoiceHeader,
            String buyersName,
            String buyersTIN,
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
            output.write(ESC_DOUBLE_SIZE_ON);
            output.write(companyName.getBytes(CHARSET));
            output.write(ESC_DOUBLE_SIZE_OFF);
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
            printDivider(output);
            
            // Transaction Details - left-right aligned
            printFormattedLine(output, "Receipt#", invoiceHeader.getInvoiceNumber(), 0);
            printFormattedLine(output, "Date", currentDateTime, 0);
            printFormattedLine(output, "Customer", buyersName, 0);
            printFormattedLine(output, "Buyer TIN", buyersTIN, 0);
            
            // Divider before items
            printDivider(output);
            
            // Table header for items
            String header = String.format("%-15s %-8s %5s %8s", "Item", "Unit", "Qty", "Amount");
            output.write(header.getBytes(CHARSET));
            output.write(LF);
            
            // Divider again
            printDivider(output);
            
            // Line items with proper formatting
            for (LineItemDto item : lineItems) {
                // Item description - truncate if too long
                String description = item.getDescription().length() > 15 ? 
                    item.getDescription().substring(0, 15) : item.getDescription();
                
                String itemLine = String.format("%-15s %-8s %5s %8s", 
                    description,
                    formatCurrency(item.getUnitPrice()),
                    item.getQuantity(),
                    formatCurrency(item.getQuantity() * item.getUnitPrice())
                );
                
                output.write(itemLine.getBytes(CHARSET));
                output.write(LF);
            }
            
            // Divider before totals
            printDivider(output);
            
            // Calculate totals
            double totalVAT = invoiceTaxBreakDown.stream().mapToDouble(TaxBreakDown::getTaxAmount).sum();
            double subtotal = invoiceTaxBreakDown.stream().mapToDouble(TaxBreakDown::getTaxableAmount).sum();
            double invoiceTotal = subtotal + totalVAT;
            
            // Print totals with left-right alignment
            printFormattedLine(output, "Subtotal", formatCurrency(subtotal), 0);
            
            // Tax breakdowns
            for (TaxBreakDown tax : invoiceTaxBreakDown) {
                printFormattedLine(output, "VAT " + tax.getRateId() + "%", formatCurrency(tax.getTaxAmount()), 0);
            }
            
            // Total, emphasized
            output.write(ESC_EMPHASIZE_ON);
            printFormattedLine(output, "TOTAL", formatCurrency(invoiceTotal), 0);
            output.write(ESC_EMPHASIZE_OFF);
            
            // Payment information
            printFormattedLine(output, "Amount Paid", formatCurrency(amountTendered), 0);
            
            if (change > 0) {
                printFormattedLine(output, "Change", formatCurrency(change), 0);
            }
            
            output.write(LF);
            
            // Validation section
            printDivider(output);
            
            output.write(ESC_EMPHASIZE_ON);
            output.write("*** VALIDATE YOUR RECEIPT ***".getBytes(CHARSET));
            output.write(ESC_EMPHASIZE_OFF);
            output.write(LF);
            
            // QR Code
            printQRCode(output, validationUrl);
            
            // Footer
            output.write(LF);
            printDivider(output);
            
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
     * Print a formatted line with label and value aligned left-right
     */
    private static void printFormattedLine(ByteArrayOutputStream output, String label, String value, int labelWidth) throws IOException {
        // Calculate spacing to push value to the right edge
        int totalSpacing = RECEIPT_WIDTH - label.length() - value.length();
        
        // Create the line with proper spacing
        StringBuilder line = new StringBuilder();
        line.append(label);
        
        // Add spaces to push value to right edge
        for (int i = 0; i < totalSpacing; i++) {
            line.append(" ");
        }
        
        line.append(value);
        
        output.write(line.toString().getBytes(CHARSET));
        output.write(LF);
    }
    
    /**
     * Print a divider line
     */
    private static void printDivider(ByteArrayOutputStream output) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RECEIPT_WIDTH; i++) {
            sb.append("-");
        }
        output.write(sb.toString().getBytes(CHARSET));
        output.write(LF);
    }
    
    /**
     * Format currency value
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
            String qrData = data.length() > 300 ? data.substring(0, 300) : data;
            
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
}