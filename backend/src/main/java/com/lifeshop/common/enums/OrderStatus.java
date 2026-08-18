package com.lifeshop.common.enums;

public interface OrderStatus {
    int WAIT_PAY = 0;
    int WAIT_DELIVER = 1;
    int WAIT_RECEIVE = 2;
    int FINISHED = 3;
    int CANCELED = 4;
    int REFUNDING = 5;
    int REFUNDED = 6;
}
