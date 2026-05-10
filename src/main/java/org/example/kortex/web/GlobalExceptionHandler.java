package org.example.kortex.web;

import org.example.kortex.orders.domain.exception.ProductZeroException;
import org.example.kortex.orders.domain.exception.UserNotCourierException;
import org.example.kortex.roleRequest.domain.exception.PendingRequestException;
import org.example.kortex.users.domain.exception.CourierHasActiveOrderException;
import org.example.kortex.users.domain.exception.IncorrectUpdateRoleException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler implements ErrorController {//TODO Поменять ретурны у моих исключениях

    @ExceptionHandler(ProductZeroException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductZeroException(ProductZeroException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Продукт закончился на сладе",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(UserNotCourierException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotCourierException(UserNotCourierException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Это доступно только курьерам",
                e.getMessage(),
                LocalDateTime.now()
        );

        return  ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(PendingRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handlePendingRequestException(PendingRequestException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "У вас уже есть активаная заявка",
                e.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(CourierHasActiveOrderException.class)
    public ResponseEntity<ErrorResponseDTO> handleCourierHasActiveOrderException(CourierHasActiveOrderException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "У вас уже есть активный заказ",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(IncorrectUpdateRoleException.class)
    public ResponseEntity<ErrorResponseDTO> handleIncorrectUpdateRoleException(IncorrectUpdateRoleException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Вы не можете поменять роль на выбранную",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            NoSuchElementException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Не получилось найти данные",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(Exception e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Не правильно переданные данные",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "Ошибка сервера,попробуй ещё раз",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
