package com.daangcool.stack.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    private ErrorConstants() {}

    public static final URI DEFAULT_TYPE = URI.create("https://stack-app.com/probs/problem-with-message");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create("https://stack-app.com/probs/login-used");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create("https://stack-app.com/probs/email-used");
    public static final URI INVALID_PASSWORD_TYPE = URI.create("https://stack-app.com/probs/invalid-password");
}
