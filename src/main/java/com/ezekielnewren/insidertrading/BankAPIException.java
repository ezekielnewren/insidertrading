package com.ezekielnewren.insidertrading;

public class BankAPIException extends RuntimeException {

    Reason reason;

    enum Reason {
        GENERAL_ERROR,
        NOT_LOGGED_IN,
        NOT_PERMITTED,
        NO_SUCH_FUNCTION,
        NO_SUCH_USERNAME,
        LOGIN_NO_SUCH_USERNAME,
        LOGIN_FAILED,
        ALREADY_LOGGED_IN,
        REGISTRATION_USERNAME_NOT_AVAILABLE,
        REGISTRATION_FAILED,
        NOT_ENOUGH_MONEY,
        ILLEGAL_ACCESS;

        @Override
        public String toString() {
            return "{\"errorCode\": "+this.ordinal()+", \"message\": \""+this.name()+"\"}";
        }
    }

    public BankAPIException(Reason r) {
        this.reason = r;
        r.toString();
    }

    @Override
    public String getMessage() {
        return reason.toString();
    }

}
