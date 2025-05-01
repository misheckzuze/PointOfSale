package com.pointofsale;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.helper.Helper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class EscPosReceiptPrinter {

    private static final byte[] INIT_PRINTER = new byte[] { 0x1B, 0x40 };
    private static final byte[] CUT_PAPER = new byte[] { 0x1D, 0x56, 0x00 };
    private static final byte[] OPEN_DRAWER = new byte[] { 0x1B, 0x70, 0x00, 0x32, (byte) 0xFA };
    private static final String CHARSET = "UTF-8";

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

        output.write(INIT_PRINTER);
        output.write(centerText("*** START OF LEGAL RECEIPT ***").getBytes(CHARSET));
        output.write(newLine());

        output.write(("TIN: " + tin + "\n").getBytes(CHARSET));
        output.write(("Buyer: " + buyersName + "\n").getBytes(CHARSET));
        output.write(("Receipt #: " + invoiceHeader.getInvoiceNumber() + "\n").getBytes(CHARSET));
        output.write("------------------------------------------\n".getBytes(CHARSET));

        for (LineItemDto item : lineItems) {
            output.write((item.getQuantity() + " x " + formatValue(item.getUnitPrice()) + "\n").getBytes(CHARSET));
            output.write((item.getDescription() + "\n").getBytes(CHARSET));
        }

        output.write("------------------------------------------\n".getBytes(CHARSET));

        double totalVAT = invoiceTaxBreakDown.stream().mapToDouble(TaxBreakDown::getTaxAmount).sum();
        double invoiceTotal = invoiceTaxBreakDown.stream().mapToDouble(t -> t.getTaxAmount() + t.getTaxableAmount()).sum();

        output.write(("TOTAL VAT: " + formatValue(totalVAT) + "\n").getBytes(CHARSET));
        output.write(("TOTAL: " + formatValue(invoiceTotal) + "\n").getBytes(CHARSET));
        output.write(("Tendered: " + formatValue(amountTendered) + "\n").getBytes(CHARSET));
        if (change > 0) {
            output.write(("CHANGE: " + formatValue(change) + "\n").getBytes(CHARSET));
        }

        output.write(("DATE: " + java.time.LocalDateTime.now().toString() + "\n").getBytes(CHARSET));

        // Generate and print QR code
        output.write(newLine());
        output.write(alignCenter());
        BufferedImage qrImage = generateQRCodeImage(validationUrl, 200, 200);
        byte[] qrBytes = convertBufferedImageToByteArray(qrImage);
        output.write(qrBytes);

        output.write(newLine());
        output.write(centerText("*** END OF LEGAL RECEIPT ***").getBytes(CHARSET));
        output.write(newLine());

        // ESC/POS Commands
        output.write(OPEN_DRAWER);
        output.write(CUT_PAPER);

        byte[] data = output.toByteArray();

        // Send to printer
        sendToPrinter(data);
    }

    private static void sendToPrinter(byte[] data) throws PrintException {
        DocPrintJob job = findPrintService().createPrintJob();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(data, flavor, null);
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        job.print(doc, attrs);
    }

    private static PrintService findPrintService() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services.length == 0) throw new RuntimeException("No printer found!");
        return services[0]; // use first available or prompt for selection
    }

    private static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private static byte[] convertBufferedImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "PNG", out);  // Writing the image as PNG
        return out.toByteArray();
    }

    private static String centerText(String text) {
        return "\u001b\u0061\u0001" + text + "\n"; // ESC a 1 (center)
    }

    private static byte[] newLine() {
        return new byte[] { 0x0A };
    }

    private static byte[] alignCenter() {
        return new byte[] { 0x1B, 0x61, 0x01 }; // ESC a 1
    }

    private static String formatValue(double value) {
        return String.format("MWK %,.2f", value);
    }
}
