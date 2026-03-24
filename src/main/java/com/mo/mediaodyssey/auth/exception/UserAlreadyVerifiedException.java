package com.mo.mediaodyssey.auth.exception;

import org.springframework.security.authentication.AccountStatusException;

public class UserAlreadyVerifiedException extends AccountStatusException {

    public UserAlreadyVerifiedException(String msg) {
        super(msg);
    }

}
