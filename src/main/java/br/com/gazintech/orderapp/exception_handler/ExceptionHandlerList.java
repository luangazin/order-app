package br.com.gazintech.orderapp.exception_handler;

public interface ExceptionHandlerList {
    /**
     * Returns a list of all exception handler items.
     *
     * @return List of ExceptionHandlerItem
     */
    java.util.List<ExceptionHandlerItem> getExceptionHandler();

    /**
     * Finds an exception handler item by the given exception.
     *
     * @param exception The exception to find the handler for.
     * @return ExceptionHandlerItem corresponding to the exception.
     */
    ExceptionHandlerItem findByException(Throwable exception);
}
