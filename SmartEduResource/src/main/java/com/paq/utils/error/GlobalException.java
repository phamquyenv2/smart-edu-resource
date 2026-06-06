package com.paq.utils.error;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.paq.pojo.response.ResResponse;

import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        List<String> errors = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Object message = errors.isEmpty()
                ? "Dữ liệu không hợp lệ"
                : (errors.size() == 1 ? errors.get(0) : errors);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResResponse<Object>> handleInvalidJson(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Request Body", "JSON body không hợp lệ");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResResponse<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing Parameter",
                "Thiếu tham số bắt buộc: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        return buildResponse(HttpStatus.BAD_REQUEST, "Type Mismatch",
                "Tham so '" + ex.getName() + "' phai co kieu " + typeName);
    }

    @ExceptionHandler(value = {
        UsernameNotFoundException.class,
        IdInvalidException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ResResponse<Object>> handleBadRequestException(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(value = {
        AuthenticationException.class,
        BadCredentialsException.class
    })
    public ResponseEntity<ResResponse<Object>> handleAuthenticationException(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bạn chưa đăng nhập hoặc thông tin đăng nhập không chính xác";
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    @ExceptionHandler(value = {
        AccessDeniedException.class,
        PermissionException.class,
        SecurityException.class
    })
    public ResponseEntity<ResResponse<Object>> handleAccessDeniedException(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bạn không có quyền truy cập";
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", message);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ResResponse<Object>> handleStorageException(StorageException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Exception upload file", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResResponse<Object>> handleNotFoundException(Exception ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", "URL không tồn tại");
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<ResResponse<Object>> handleNoResultException(NoResultException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", "Dữ liệu không tồn tại");
    }

    @ExceptionHandler(value = {
        DataIntegrityViolationException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<ResResponse<Object>> handleDataIntegrityViolation(Exception ex) {
        return buildResponse(HttpStatus.CONFLICT, "Data Integrity Violation",
                "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc hệ thống.");
    }

    @ExceptionHandler(value = {
        ObjectOptimisticLockingFailureException.class,
        OptimisticLockException.class
    })
    public ResponseEntity<ResResponse<Object>> handleOptimisticLockingFailure(Exception ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict",
                "Dữ liệu đã bị cập nhật bởi một người dùng khác. Vui lòng làm mới trang và thử lại.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildResponse(status, ex.getStatusCode().toString(), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResResponse<Object>> handleAllException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<ResResponse<Object>> buildResponse(HttpStatus status, String error, Object message) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(status.value());
        res.setError(error);
        res.setMessage(message);

        return ResponseEntity.status(status).body(res);
    }
}
