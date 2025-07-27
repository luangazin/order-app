package br.com.gazintech.orderapp.exception;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: luan-gazin<br/>
 * Date: 27/07/2025<br/>
 * Time: 00:13<br/>
 * To change this template use File | Settings | File Templates.
 */
public class OrderNotFoundException extends OrderAppException {

    public OrderNotFoundException() {
        super("Invalid order status");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderNotFoundException(Throwable cause) {
        super(cause);
    }
}
