package com.crm.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException seller(Long id) {
        return new NotFoundException("Продавец с id=" + id + " не найден");
    }

    public static NotFoundException transaction(Long id) {
        return new NotFoundException("Транзакция с id=" + id + " не найдена");
    }
}
