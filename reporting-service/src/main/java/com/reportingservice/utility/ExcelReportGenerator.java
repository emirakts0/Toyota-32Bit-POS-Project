package com.reportingservice.utility;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleItemDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;


public class ExcelReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(ExcelReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd   HH:mm:ss");

    public static ByteArrayInputStream generateSalesExcelFile(List<SaleDto> sales) {
        log.trace("createExcel method begins. Number of sales: {}", sales.size());

        sales.sort(Comparator.comparing(SaleDto::getSaleDate, Comparator.nullsFirst(LocalDateTime::compareTo)));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Style for rows
            CellStyle oddRowStyle = workbook.createCellStyle();
            oddRowStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            oddRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            oddRowStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(oddRowStyle);

            CellStyle evenRowStyle = workbook.createCellStyle();
            evenRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            evenRowStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(evenRowStyle);

            // Style for header
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            setBorders(headerStyle);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);


            // Sales sheet
            log.trace("Creating Sales sheet");
            Sheet salesSheet = workbook.createSheet("Sales");
            Row salesHeaderRow = salesSheet.createRow(0);
            String[] salesHeaders = {"ID", "Cashier Name", "Total Price", "Discounted Price", "Campaign Name",
                    "Campaign ID", "Discount Type", "Discount Value", "Amount Received", "Change",
                    "Payment Method", "Sale Date", "Is Cancelled"};
            setupSheetHeader(salesHeaders, salesHeaderRow, headerStyle, salesSheet);

            // Items sheet
            log.trace("Creating Items sheet");
            Sheet itemsSheet = workbook.createSheet("Items");
            Row itemsHeaderRow = itemsSheet.createRow(0);
            String[] itemHeaders = {"Sale ID", "Item Barcode", "Item Name", "Item Quantity", "Item Sale Price"};
            setupSheetHeader(itemHeaders, itemsHeaderRow, headerStyle, itemsSheet);

            int salesRowIdx = 1;
            int itemsRowIdx = 1;
            boolean useOddStyle = true;

            for (SaleDto sale : sales) {
                Row salesRow = salesSheet.createRow(salesRowIdx);
                CellStyle style = (salesRowIdx % 2 == 0) ? evenRowStyle : oddRowStyle;

                createCell(salesRow, 0, sale.getId(), style);
                createCell(salesRow, 1, sale.getCashierName(), style);
                createCell(salesRow, 2, sale.getTotalPrice() != null ? sale.getTotalPrice().doubleValue() : 0, style);
                createCell(salesRow, 3, sale.getDiscountedPrice() != null ? sale.getDiscountedPrice().doubleValue() : 0, style);
                createCell(salesRow, 4, sale.getCampaignName(), style);
                createCell(salesRow, 5, sale.getCampaignId(), style);
                createCell(salesRow, 6, sale.getDiscountType() != null ? sale.getDiscountType().getText() : "N/A", style);
                createCell(salesRow, 7, sale.getDiscountValue(), style);
                createCell(salesRow, 8, sale.getAmountReceived() != null ? sale.getAmountReceived().doubleValue() : 0, style);
                createCell(salesRow, 9, sale.getChange() != null ? sale.getChange().doubleValue() : 0, style);
                createCell(salesRow, 10, sale.getPaymentMethod() != null ? sale.getPaymentMethod().getText() : "N/A", style);
                createCell(salesRow, 11, sale.getSaleDate() != null ? sale.getSaleDate().format(DATE_FORMATTER) : "N/A", style);
                createCell(salesRow, 12, sale.isCancelled() ? "true" : "false", style);

                salesRowIdx++;

                for (SaleItemDto item : sale.getSaleItems()) {
                    Row itemRow = itemsSheet.createRow(itemsRowIdx);
                    CellStyle itemStyle = useOddStyle ? oddRowStyle : evenRowStyle;

                    createCell(itemRow, 0, sale.getId(), itemStyle);
                    createCell(itemRow, 1, item.getBarcode(), itemStyle);
                    createCell(itemRow, 2, item.getName(), itemStyle);
                    createCell(itemRow, 3, item.getQuantity(), itemStyle);
                    createCell(itemRow, 4, item.getSalePrice() != null ? item.getSalePrice().doubleValue() : 0, itemStyle);

                    itemsRowIdx++;
                }
                useOddStyle = !useOddStyle;
            }

            workbook.write(out);
            log.trace("generateSalesReport method ends.");
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Failed to create Excel file", e);
            throw new RuntimeException("Failed to create Excel file", e);
        }
    }


    private static void setupSheetHeader(String[] headers, Row headerRow, CellStyle headerStyle, Sheet sheet) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);

            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 256);
        }
    }

    private static void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        }
        cell.setCellStyle(style);
    }

    private static void setBorders(CellStyle style) {
//        style.setBorderTop(BorderStyle.THIN);
//        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}