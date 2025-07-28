package br.com.gazintech.orderapp.exception;


public class PartnerNotFoundException extends OrderAppException {

    public PartnerNotFoundException() {
        super("Invalid order status");
    }

    public PartnerNotFoundException(String message) {
        super(message);
    }

    public PartnerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartnerNotFoundException(Throwable cause) {
        super(cause);
    }
}
