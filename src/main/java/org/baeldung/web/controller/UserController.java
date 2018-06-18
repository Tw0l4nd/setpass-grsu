package org.baeldung.web.controller;

import java.util.Locale;

import org.baeldung.persistence.dao.VerificationTokenRepository;
import org.baeldung.persistence.model.User;
import org.baeldung.security.ActiveUserStore;
import org.baeldung.security.google2fa.CustomWebAuthenticationDetails;
import org.baeldung.service.IUserService;
import org.baeldung.service.ldap.LdapService;
import org.baeldung.web.util.GenericResponse;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    @Autowired
    ActiveUserStore activeUserStore;

    @Autowired
    IUserService userService;

    @Autowired
    LdapService ldapService;

    @RequestMapping(value = "/loggedUsers", method = RequestMethod.GET)
    public String getLoggedUsers(final Locale locale, final Model model) {
        model.addAttribute("users", activeUserStore.getUsers());
        return "users";
    }

    @RequestMapping(value = "/loggedUsersFromSessionRegistry", method = RequestMethod.GET)
    public String getLoggedUsersFromSessionRegistry(final Locale locale, final Model model) {
        model.addAttribute("users", userService.getUsersFromSessionRegistry());
        return "users";
    }

    @RequestMapping(value = "/setPass", method = RequestMethod.POST)
    public String changeUserPass(HttpServletRequest request, final Locale locale, final Model model/*, @RequestParam("token") final String token*/) {
        String verificationCode = request.getParameter("code");
        String password = request.getParameter("password");
        String token = request.getParameter("token");
        User user = userService.getUser(token);

        final Totp totp = new Totp(user.getSecret());
        if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
            return "redirect:/failPasswordChange.html?lang=" + locale.getLanguage();
        }

        ResponseEntity<String> responseEntity;

        try {
            responseEntity = ldapService.setNewPass(user.getLogin(), password);
            if (responseEntity== null || !responseEntity.getStatusCode().toString().equals("200")) {
                throw new IllegalStateException("bad creds");
            }
        } catch (Exception e) {
            return "redirect:/failPasswordChange.html?lang=" + locale.getLanguage();
        }

        if (!responseEntity.getStatusCode().toString().equals("200")) {
            return "redirect:/failPasswordChange.html?lang=" + locale.getLanguage();
        }

        return "redirect:/successPasswordChange.html?lang=" + locale.getLanguage();
    }

    @RequestMapping(value = "/setPass", method = RequestMethod.GET)
    public String returnPagechangeUserPass(final Locale locale, final Model model, @RequestParam("token") final String token) {
//        model.addAttribute("users", userService.getUsersFromSessionRegistry());

        return "setPass";
    }


    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }
}
