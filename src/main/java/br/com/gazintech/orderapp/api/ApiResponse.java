package br.com.gazintech.orderapp.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "error-code", "error-message", "body"})
public class ApiResponse<T> {

    @JsonProperty("body")
    private T body;

    @JsonProperty("error-code")
    private String errorCode;

    @JsonProperty("error-message")
    private String errorMessage;

    @JsonProperty("status")
    private Status status = Status.SUCCESS;

    @JsonProperty("pagination")
    private Pagination pagination;

    public enum Status {
        SUCCESS, ERROR
    }

    @Getter
    @JsonPropertyOrder({"total-items", "total-pages", "current-page"})
    public static class Pagination {
        @JsonProperty("total-items")
        private Long totalItems;

        @JsonProperty("total-pages")
        private Integer totalPages;

        @JsonProperty("current-page")
        private Integer currentPage;

        public Pagination(Long totalItems, Integer totalPages, Integer currentPage) {
            this.totalItems = totalItems;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }
    }

    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    @Override
    public String toString() {
        return "ApiResponse(body=" + body + ", errorMessage=" + errorMessage + ", status=" + status + ")";
    }

    public static class ApiResponseBuilder<T> {
        private T body;
        private String errorCode;
        private String errorMessage;
        private ApiResponse.Status status;
        private HttpStatus httpStatus = HttpStatus.OK;
        private Long paginationTotalItems;
        private Integer paginationTotalPages;
        private Integer paginationCurrentPage;

        public ApiResponseBuilder<T> body(T body) {
            this.body = body;
            return this;
        }

        public ApiResponseBuilder<T> errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ApiResponseBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ApiResponseBuilder<T> status(ApiResponse.Status status) {
            this.status = status;
            return this;
        }

        public ApiResponseBuilder<T> success() {
            this.status = ApiResponse.Status.SUCCESS;
            this.httpStatus = HttpStatus.OK;
            return this;
        }

        public ApiResponseBuilder<T> success(T body) {
            return success(body, HttpStatus.OK);
        }

        public ApiResponseBuilder<T> success(T body, HttpStatus httpStatus) {
            this.body = body;
            this.status = ApiResponse.Status.SUCCESS;
            this.httpStatus = httpStatus;
            return this;
        }

        public ApiResponseBuilder<T> error() {
            this.status = ApiResponse.Status.ERROR;
            this.httpStatus = HttpStatus.BAD_REQUEST;
            return this;
        }

        public ApiResponseBuilder<T> error(String errorCode, String errorMessage) {
            return error(errorCode, errorMessage, HttpStatus.BAD_REQUEST);
        }

        public ApiResponseBuilder<T> error(String errorCode, String errorMessage, HttpStatus httpStatus) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.status = ApiResponse.Status.ERROR;
            this.httpStatus = httpStatus;
            return this;
        }

        public ApiResponseBuilder<T> httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public ApiResponseBuilder<T> pagination(long totalItems, int totalPages, int currentPage) {
            this.paginationTotalItems = totalItems;
            this.paginationTotalPages = totalPages;
            this.paginationCurrentPage = currentPage;
            return this;
        }

        public ApiResponseBuilder<T> pagination(Page<?> page) {
            if (page == null) {
                return this;
            }
            this.paginationTotalItems = page.getTotalElements();
            this.paginationTotalPages = page.getTotalPages();
            this.paginationCurrentPage = page.getNumber() + 1;
            return this;
        }

        public ResponseEntity<ApiResponse<T>> build() {
            ApiResponse<T> response = new ApiResponse<>();
            response.setBody(body);
            response.setErrorCode(errorCode);
            response.setErrorMessage(errorMessage);
            response.setStatus(status != null ? status : ApiResponse.Status.SUCCESS);
            if (paginationTotalItems != null || paginationTotalPages != null || paginationCurrentPage != null) {
                response.setPagination(new Pagination(paginationTotalItems, paginationTotalPages, paginationCurrentPage));
            }
            return ResponseEntity.status(httpStatus).body(response);
        }
    }
}
