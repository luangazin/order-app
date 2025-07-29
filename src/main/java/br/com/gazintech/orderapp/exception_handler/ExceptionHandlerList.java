package br.com.gazintech.orderapp.exception_handler;

/**
 * Interface for managing a list of exception handlers.
 * It provides methods to retrieve all exception handler items and to find a specific handler by exception type.
 */
public interface ExceptionHandlerList {
    /**
     * Returns a list of all exception handler items.
     *
     * @return List of ExceptionHandlerItem
     */
    java.util.List<ExceptionHandlerItem> getExceptionHandler();

    /**
     * Finds an exception handler item by the given exception class.
     *
     * @param exceptionClass The class of the exception to find the handler for.
     * @return ExceptionHandlerItem corresponding to the exception class.
     */
    ExceptionHandlerItem findByClass(Class<? extends Throwable> exceptionClass);
}
