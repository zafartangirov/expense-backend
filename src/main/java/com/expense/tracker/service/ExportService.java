package com.expense.tracker.service;

import com.expense.tracker.entity.Expense;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // ===== EXCEL =====
    public byte[] exportToExcel(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        List<Expense> expenses = expenseRepository
                .findByUserIdOrderByDateDesc(user.getId());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Xarajatlar");

            // Ustun kengliklari
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 8000);

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Amount style
            CellStyle amountStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"#", "Sarlavha", "Summa", "Kategoriya", "Sana", "Tavsif"};
            sheet.setColumnWidth(5, 8000);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ma'lumot qatorlari
            int rowNum = 1;
            double total = 0;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(expense.getTitle());

                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(expense.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);

                row.createCell(3).setCellValue(
                        expense.getCategory() != null
                                ? expense.getCategory().getName() : "—");
                row.createCell(4).setCellValue(
                        expense.getDate().format(DATE_FORMAT));
                row.createCell(5).setCellValue(
                        expense.getDescription() != null
                                ? expense.getDescription() : "—");

                total += expense.getAmount().doubleValue();
            }

            // Jami qator
            Row totalRow = sheet.createRow(rowNum + 1);
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);

            Cell totalLabel = totalRow.createCell(1);
            totalLabel.setCellValue("JAMI:");
            totalLabel.setCellStyle(totalStyle);

            Cell totalCell = totalRow.createCell(2);
            totalCell.setCellValue(total);
            totalCell.setCellStyle(amountStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ===== PDF =====
    public byte[] exportToPdf(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        List<Expense> expenses = expenseRepository
                .findByUserIdOrderByDateDesc(user.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Sarlavha
        com.itextpdf.text.Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        Paragraph title = new Paragraph("Xarajatlar Hisoboti", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Foydalanuvchi info
        com.itextpdf.text.Font infoFont = FontFactory.getFont(
                FontFactory.HELVETICA, 11, BaseColor.GRAY);
        Paragraph info = new Paragraph(
                "Foydalanuvchi: " + user.getFullName() +
                        " | Email: " + user.getEmail(), infoFont);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(20);
        document.add(info);

        // Jadval
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 2f, 2f, 2f});
        table.setSpacingBefore(10f);

        // Jadval header
        String[] headers = {"#", "Sarlavha", "Summa", "Kategoriya", "Sana"};
        com.itextpdf.text.Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new BaseColor(99, 102, 241));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Jadval qatorlari
        com.itextpdf.text.Font rowFont = FontFactory.getFont(
                FontFactory.HELVETICA, 10, BaseColor.BLACK);
        com.itextpdf.text.Font altFont = FontFactory.getFont(
                FontFactory.HELVETICA, 10, BaseColor.BLACK);

        double total = 0;
        int i = 1;
        for (Expense expense : expenses) {
            BaseColor bgColor = (i % 2 == 0)
                    ? new BaseColor(245, 245, 255)
                    : BaseColor.WHITE;

            addCell(table, String.valueOf(i), rowFont, bgColor, Element.ALIGN_CENTER);
            addCell(table, expense.getTitle(), rowFont, bgColor, Element.ALIGN_LEFT);
            addCell(table,
                    String.format("%,.0f so'm", expense.getAmount().doubleValue()),
                    rowFont, bgColor, Element.ALIGN_RIGHT);
            addCell(table,
                    expense.getCategory() != null
                            ? expense.getCategory().getName() : "—",
                    rowFont, bgColor, Element.ALIGN_CENTER);
            addCell(table,
                    expense.getDate().format(DATE_FORMAT),
                    rowFont, bgColor, Element.ALIGN_CENTER);

            total += expense.getAmount().doubleValue();
            i++;
        }

        // Jami qator
        com.itextpdf.text.Font totalFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        BaseColor totalBg = new BaseColor(99, 102, 241);

        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setColspan(2);
        emptyCell.setBackgroundColor(totalBg);
        emptyCell.setPadding(8);
        table.addCell(emptyCell);

        addCell(table,
                String.format("JAMI: %,.0f so'm", total),
                totalFont, totalBg, Element.ALIGN_RIGHT);

        PdfPCell empty2 = new PdfPCell(new Phrase(""));
        empty2.setColspan(2);
        empty2.setBackgroundColor(totalBg);
        empty2.setPadding(8);
        table.addCell(empty2);

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private void addCell(PdfPTable table, String text,
                         com.itextpdf.text.Font font,
                         BaseColor bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(align);
        cell.setPadding(7);
        table.addCell(cell);
    }
}