package com.furniture.service;

import com.furniture.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public void exportMaterialsToExcel(List<Material> materials, HttpServletResponse response) throws IOException {
        String filename = "materials_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Материалы");

            // Заголовки
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Артикул", "Наименование", "Ед. изм.", "Цена", "Остаток", "Мин. остаток"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            // Данные
            int rowNum = 1;
            for (Material m : materials) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getSku());
                row.createCell(2).setCellValue(m.getName());
                row.createCell(3).setCellValue(m.getUnit());
                row.createCell(4).setCellValue(m.getPrice().doubleValue());
                row.createCell(5).setCellValue(m.getCurrentBalance().doubleValue());
                row.createCell(6).setCellValue(m.getMinBalance().doubleValue());
            }

            // Автоширина колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void exportProductsToExcel(List<FinishedProduct> products, HttpServletResponse response) throws IOException {
        String filename = "products_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Продукция");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Артикул", "Наименование", "Категория", "Ед. изм.", "Цена", "Остаток", "Себестоимость"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            int rowNum = 1;
            for (FinishedProduct p : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getSku());
                row.createCell(2).setCellValue(p.getName());
                row.createCell(3).setCellValue(p.getCategory() != null ? p.getCategory().getName() : "");
                row.createCell(4).setCellValue(p.getUnit());
                row.createCell(5).setCellValue(p.getSellingPrice().doubleValue());
                row.createCell(6).setCellValue(p.getCurrentBalance().doubleValue());
                row.createCell(7).setCellValue(p.getCostPrice() != null ? p.getCostPrice().doubleValue() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void exportClientsToExcel(List<Client> clients, HttpServletResponse response) throws IOException {
        String filename = "clients_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Клиенты");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Наименование", "ИНН", "Телефон", "Контактное лицо", "Адрес"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            int rowNum = 1;
            for (Client c : clients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getName());
                row.createCell(2).setCellValue(c.getInn() != null ? c.getInn() : "");
                row.createCell(3).setCellValue(c.getPhone() != null ? c.getPhone() : "");
                row.createCell(4).setCellValue(c.getContactPerson() != null ? c.getContactPerson() : "");
                row.createCell(5).setCellValue(c.getAddress() != null ? c.getAddress() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void exportProductionOrdersToExcel(List<ProductionOrder> orders, HttpServletResponse response) throws IOException {
        String filename = "production_orders_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Производственные заказы");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "№ заказа", "Продукция", "План. количество", "Факт. количество", "Статус", "План. дата", "Дата завершения"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            int rowNum = 1;
            for (ProductionOrder o : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(o.getId());
                row.createCell(1).setCellValue(o.getOrderNumber());
                row.createCell(2).setCellValue(o.getProduct().getName());
                row.createCell(3).setCellValue(o.getPlannedQuantity().doubleValue());
                row.createCell(4).setCellValue(o.getActualQuantity() != null ? o.getActualQuantity().doubleValue() : 0);
                row.createCell(5).setCellValue(getStatusRu(o.getStatus()));
                row.createCell(6).setCellValue(o.getPlannedDate().toString());
                row.createCell(7).setCellValue(o.getCompletedDate() != null ? o.getCompletedDate().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public void exportShipmentsToExcel(List<Shipment> shipments, HttpServletResponse response) throws IOException {
        String filename = "shipments_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отгрузки");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "№ отгрузки", "Дата", "Клиент", "Продукция", "Количество", "Цена", "Сумма"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            int rowNum = 1;
            for (Shipment s : shipments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getShipmentNumber());
                row.createCell(2).setCellValue(s.getShipmentDate().toString());
                row.createCell(3).setCellValue(s.getClient().getName());
                row.createCell(4).setCellValue(s.getProduct().getName());
                row.createCell(5).setCellValue(s.getQuantity().doubleValue());
                row.createCell(6).setCellValue(s.getPrice().doubleValue());
                row.createCell(7).setCellValue(s.getTotalAmount().doubleValue());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String getStatusRu(String status) {
        return switch (status) {
            case "planned" -> "Планируется";
            case "in_progress" -> "В работе";
            case "completed" -> "Завершён";
            case "cancelled" -> "Отменён";
            default -> status;
        };
    }
}