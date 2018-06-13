package org.baeldung.security.google2fa;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;

    private final String verificationCode;
    private String loginPage = "-1";

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        verificationCode = request.getParameter("code");
        loginPage = request.getParameter("loginPage");
    }

    public String getVerificationCode() {
        return verificationCode;
    }
}