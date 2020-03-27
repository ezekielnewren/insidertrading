package com.ezekielnewren.insidertrading;

public class BankAPIException extends RuntimeException {

    Reason reason;

    enum Reason {
        GENERAL_ERROR,
        NOT_LOGGED_IN,
        NOT_PERMITTED,
        NO_SUCH_FUNCTION,
        ILLEGAL_ACCESS;

        @Override
        public String toString() {
            return "{\"code\": "+this.ordinal()+", \"msg\": \""+this.name()+"\"}";
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
