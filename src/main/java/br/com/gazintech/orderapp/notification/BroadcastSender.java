package br.com.gazintech.orderapp.notification;


public interface BroadcastSender {

    /**
     * Sends a notification with the given message and recipient.
     *
     * @param message   the message to be sent
     * @param recipient the recipient of the notification
     */
    void sendNotification(Object message, String recipient);
}
