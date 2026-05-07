package com.furniture.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка ошибок целостности данных (внешние ключи, уникальность)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                               RedirectAttributes redirectAttributes,
                                               HttpServletRequest request) {
        String message = "Невозможно выполнить операцию из-за нарушения целостности данных";

        String errorMessage = ex.getMostSpecificCause().getMessage();

        if (errorMessage.contains("products_sku_key")) {
            message = "Товар с таким артикулом уже существует";
        } else if (errorMessage.contains("materials_sku_key")) {
            message = "Материал с таким артикулом уже существует";
        } else if (errorMessage.contains("users_username_key")) {
            message = "Пользователь с таким логином уже существует";
        } else if (errorMessage.contains("stock_movements_product_id_fkey")) {
            message = "Невозможно удалить товар, так как он используется в документах";
        } else if (errorMessage.contains("warehouse_documents_counterparty_id_fkey")) {
            message = "Невозможно удалить контрагента, так как он участвует в документах";
        } else if (errorMessage.contains("shipments_client_id_fkey")) {
            message = "Невозможно удалить клиента, так как у него есть отгрузки";
        } else if (errorMessage.contains("production_orders_product_id_fkey")) {
            message = "Невозможно удалить продукцию, так как она используется в заказах";
        }

        redirectAttributes.addFlashAttribute("error", message);

        // Определяем, откуда пришёл запрос
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    // Обработка всех остальных исключений
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUrl", request.getRequestURL());
        model.addAttribute("timestamp", java.time.LocalDateTime.now());

        // Логируем ошибку
        System.err.println("Ошибка: " + ex.getMessage());
        ex.printStackTrace();

        return "error/error";
    }
}