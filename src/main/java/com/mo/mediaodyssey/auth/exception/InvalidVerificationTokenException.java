package com.mo.mediaodyssey.auth.exception;

import org.springframework.security.authentication.AccountStatusException;

public class InvalidVerificationTokenException extends AccountStatusException {

    public InvalidVerificationTokenException(String msg) {
        super(msg);
    }

}
