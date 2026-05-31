package com.draftlab.common.events;

public final class Topics {
    public static final String ORDER_CREATED = "orders.created";
    public static final String PAYMENT_AUTHORIZED = "payments.authorized";
    public static final String PAYMENT_FAILED = "payments.failed";
    public static final String NOTIFICATION_REQUESTED = "notifications.requested";

    private Topics() {
    }
}
