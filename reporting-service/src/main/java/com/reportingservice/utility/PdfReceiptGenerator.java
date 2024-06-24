package com.reportingservice.utility;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleItemDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating PDF receipts.
 * @author Emir Aktaş
 */
public class PdfReceiptGenerator {

    private static final Logger log = LoggerFactory.getLogger(PdfReceiptGenerator.class);

    private static final String SEPARATOR = "--------------------------------------------------------------";
    private static final float POINTS_PER_MM = 2.83465f;
    private static final float RECEIPT_WIDTH_MM = 80;
    private static final float RECEIPT_HEADER_HEIGHT_MM = 55;
    private static final float DEFAULT_FOOTER_HEIGHT_MM = 25;
    private static final float EXTRA_FOOTER_HEIGHT_MM = 15;
    private static final float ITEM_HEIGHT_MM = 11.3f;
    private static final int MAX_PRODUCT_NAME_LENGTH = 25;
    private static final int FONT_SIZE = 10;


    /**
     * Creates a PDF receipt for a given sale and market information.
     *
     * @param saleDto   the sale data transfer object containing sale details
     * @param marketInfo the market information to be included in the receipt
     * @return a byte array representing the PDF receipt
     */
    public static byte[] createReceipt(SaleDto saleDto, MarketInfo marketInfo) {
        log.trace("createReceipt method begins. SaleDto: {}, MarketInfo: {}", saleDto, marketInfo);

        int itemCount = saleDto.getSaleItems().size();
        float receiptHeightMM = RECEIPT_HEADER_HEIGHT_MM + DEFAULT_FOOTER_HEIGHT_MM + (itemCount * ITEM_HEIGHT_MM);
        if (saleDto.getDiscountValue() != 0) {
            receiptHeightMM += EXTRA_FOOTER_HEIGHT_MM;
        }
        PDRectangle receiptSize = new PDRectangle(RECEIPT_WIDTH_MM * POINTS_PER_MM, receiptHeightMM * POINTS_PER_MM);
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(receiptSize);
        document.addPage(page);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            PDType0Font font = PDType0Font.load(document, new File("reporting-service/src/main/resources/fonts/Helvetica Bold TR.ttf"));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                log.trace("Starting to write content to PDF.");

                drawCenteredText(contentStream, font, marketInfo.getName(), FONT_SIZE, receiptSize.getHeight() - 20);
                drawCenteredText(contentStream, font, marketInfo.getAddress(), FONT_SIZE, receiptSize.getHeight() - 40);
                drawCenteredText(contentStream, font, marketInfo.getPhone(), FONT_SIZE, receiptSize.getHeight() - 53);
                drawCenteredText(contentStream, font, marketInfo.getCity(), FONT_SIZE, receiptSize.getHeight() - 66);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                String formattedDate = saleDto.getSaleDate().format(formatter);
                String dateText = "DATE: " + formattedDate.substring(0, 10);
                String timeText = "TIME: " + formattedDate.substring(11);

                float timeTextWidth = font.getStringWidth(timeText) / 1000 * FONT_SIZE;

                // DATE INFO
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, receiptSize.getHeight() - 86);
                contentStream.showText(dateText);
                contentStream.endText();

                // TIME INFO
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(receiptSize.getWidth() - timeTextWidth - 10, receiptSize.getHeight() - 86);
                contentStream.showText(timeText);
                contentStream.endText();

                String saleNoText = "SALE NO: " + saleDto.getId();
                String paymentMethodText = "PAYMENT: " + saleDto.getPaymentMethod().getText();

                float paymentMethodTextWidth = font.getStringWidth(paymentMethodText) / 1000 * FONT_SIZE;

                // SALE NUMBER
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, receiptSize.getHeight() - 99);
                contentStream.showText(saleNoText);
                contentStream.endText();

                // Payment method text aligned to the right
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(receiptSize.getWidth() - paymentMethodTextWidth - 10, receiptSize.getHeight() - 99);
                contentStream.showText(paymentMethodText);
                contentStream.endText();

                //CASHIER INFO
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, receiptSize.getHeight() - 112);
                contentStream.showText("CASHIER: " + saleDto.getCashierName());
                contentStream.endText();

                // SEPARATOR
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, receiptSize.getHeight() - 123);
                contentStream.showText(SEPARATOR);
                contentStream.endText();

                int yPosition = (int) receiptSize.getHeight() - 137;
                List<SaleItemDto> items = saleDto.getSaleItems();
                for (SaleItemDto item : items) {
                    String productDetails = item.getBarcode() + "   (" + item.getQuantity() + "X   " + item.getSalePrice() + ")";
                    String productName = truncateText(item.getName(), MAX_PRODUCT_NAME_LENGTH);

                    BigDecimal salePrice = item.getSalePrice().multiply(new BigDecimal(item.getQuantity()));
                    BigDecimal truncatedValue = salePrice.setScale(2, RoundingMode.DOWN);
                    String totalPriceText = truncatedValue + " TL";


                    // PRODUCT DETAILS
                    contentStream.beginText();
                    contentStream.setFont(font, FONT_SIZE);
                    contentStream.newLineAtOffset(10, yPosition);
                    contentStream.showText(productDetails);
                    contentStream.endText();

                    // PRODUCT NAME
                    contentStream.beginText();
                    contentStream.setFont(font, FONT_SIZE);
                    contentStream.newLineAtOffset(10, yPosition - 14);
                    contentStream.showText(productName);
                    contentStream.endText();

                    // Align total price to the right
                    float totalPriceWidth = font.getStringWidth(totalPriceText) / 1000 * FONT_SIZE;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(receiptSize.getWidth() - totalPriceWidth - 10, yPosition - 14);
                    contentStream.showText(totalPriceText);
                    contentStream.endText();

                    yPosition -= 32;
                }

                // SEPARATOR
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, yPosition + 1);
                contentStream.showText(SEPARATOR);
                contentStream.endText();

                // RECEIVED VALUE INFO
                yPosition -= 11;
                String receivedLabel = "Received:";
                BigDecimal receivedValueDecimal = saleDto.getAmountReceived().setScale(2, RoundingMode.DOWN);
                String receivedValue = receivedValueDecimal + " TL";
                float receivedValueWidth = font.getStringWidth(receivedValue) / 1000 * FONT_SIZE;

                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, yPosition);
                contentStream.showText(receivedLabel);
                contentStream.endText();

                // Received Value aligned to the right
                contentStream.beginText();
                contentStream.newLineAtOffset(receiptSize.getWidth() - receivedValueWidth - 10, yPosition);
                contentStream.showText(receivedValue);
                contentStream.endText();


                // CHANGE VALUE INFO
                yPosition -= 13;
                String changeLabel = "Change:";
                BigDecimal changeValueDecimal = saleDto.getChange().setScale(2, RoundingMode.DOWN);
                String changeValue = changeValueDecimal + " TL";
                float changeValueWidth = font.getStringWidth(changeValue) / 1000 * FONT_SIZE;

                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, yPosition);
                contentStream.showText(changeLabel);
                contentStream.endText();

                // Change Value aligned to the right
                contentStream.beginText();
                contentStream.newLineAtOffset(receiptSize.getWidth() - changeValueWidth - 10, yPosition);
                contentStream.showText(changeValue);
                contentStream.endText();


                // SEPARATOR
                yPosition -= 11;
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, yPosition);
                contentStream.showText(SEPARATOR);
                contentStream.endText();


                // TOTAL AMOUNT INFO
                yPosition -= 11;
                String totalAmountLabel = "Total Amount:";
                BigDecimal totalAmountDecimal = saleDto.getTotalPrice().setScale(2, RoundingMode.DOWN);
                String totalAmountValue = totalAmountDecimal + " TL";
                float totalAmountValueWidth = font.getStringWidth(totalAmountValue) / 1000 * FONT_SIZE;

                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.newLineAtOffset(10, yPosition);
                contentStream.showText(totalAmountLabel);
                contentStream.endText();

                // Total Amount Value aligned to the right
                contentStream.beginText();
                contentStream.newLineAtOffset(receiptSize.getWidth() - totalAmountValueWidth - 10, yPosition);
                contentStream.showText(totalAmountValue);
                contentStream.endText();

                if(saleDto.getDiscountValue() != 0) {
                    // DISCOUNT AMOUNT INFO
                    yPosition -= 13;
                    String discountAmountLabel = "Discount Amount:";
                    BigDecimal discountAmountDecimal = saleDto.getTotalPrice().subtract(saleDto.getDiscountedPrice()).setScale(2, RoundingMode.DOWN);
                    String discountAmountValue = discountAmountDecimal + " TL";
                    float discountAmountValueWidth = font.getStringWidth(discountAmountValue) / 1000 * FONT_SIZE;

                    contentStream.beginText();
                    contentStream.setFont(font, FONT_SIZE);
                    contentStream.newLineAtOffset(10, yPosition);
                    contentStream.showText(discountAmountLabel);
                    contentStream.endText();

                    // Discount Amount Value aligned to the right
                    contentStream.beginText();
                    contentStream.newLineAtOffset(receiptSize.getWidth() - discountAmountValueWidth - 10, yPosition);
                    contentStream.showText(discountAmountValue);
                    contentStream.endText();


                    // GRAND TOTAL INFO
                    yPosition -= 13;
                    String grandTotalLabel = "Grand Total:";
                    BigDecimal grandTotalDecimal = saleDto.getDiscountedPrice().setScale(2, RoundingMode.DOWN);
                    String grandTotalValue = grandTotalDecimal + " TL";
                    float grandTotalValueWidth = font.getStringWidth(grandTotalValue) / 1000 * FONT_SIZE;

                    contentStream.beginText();
                    contentStream.setFont(font, FONT_SIZE);
                    contentStream.newLineAtOffset(10, yPosition);
                    contentStream.showText(grandTotalLabel);
                    contentStream.endText();

                    // Grand Total Value aligned to the right
                    contentStream.beginText();
                    contentStream.newLineAtOffset(receiptSize.getWidth() - grandTotalValueWidth - 10, yPosition);
                    contentStream.showText(grandTotalValue);
                    contentStream.endText();
                }
            }

            //document.save("");
            document.save(byteArrayOutputStream);
            log.info("PDF receipt generated successfully.");
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error while creating PDF receipt.", e);
            return null;
        } finally {
            try {
                document.close();
                byteArrayOutputStream.close();
                log.trace("Document and ByteArrayOutputStream closed successfully.");
            } catch (IOException e) {
                log.error("Error while closing resources.", e);
            }
        }
    }


    /**
     * Truncates a given text to a specified maximum length, adding ellipsis if truncated.
     *
     * @param text      the text to truncate
     * @param maxLength the maximum length of the truncated text
     * @return the truncated text with ellipsis if necessary
     */
    private static String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            String truncatedText = text.substring(0, maxLength - 3) + "...";
            log.trace("Text truncated: {} to {}", text, truncatedText);
            return truncatedText;
        } else {
            return text;
        }
    }


    /**
     * Draws centered text at a specified Y position on the receipt.
     *
     * @param contentStream the content stream to draw on
     * @param font          the font to use for the text
     * @param text          the text to draw
     * @param fontSize      the font size of the text
     * @param yPosition     the Y position to draw the text at
     * @throws IOException if an error occurs while drawing the text
     */
    private static void drawCenteredText(PDPageContentStream contentStream,
                                         PDType0Font font,
                                         String text,
                                         int fontSize,
                                         float yPosition) throws IOException {
        float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        float startX = (RECEIPT_WIDTH_MM * POINTS_PER_MM - titleWidth) / 2;
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(startX, yPosition);
        contentStream.showText(text);
        contentStream.endText();
    }
}
