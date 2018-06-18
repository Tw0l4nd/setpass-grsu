package org.baeldung.web.controller;

import org.baeldung.captcha.ICaptchaService;
import org.baeldung.persistence.model.User;
import org.baeldung.registration.OnRegistrationCompleteEvent;
import org.baeldung.service.IUserService;
import org.baeldung.service.ldap.LdapService;
import org.baeldung.web.dto.UserDto;
import org.baeldung.web.util.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

@Controller
public class RegistrationCaptchaController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private IUserService userService;

    @Autowired
    private ICaptchaService captchaService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    LdapService ldapService;

    public RegistrationCaptchaController() {
        super();
    }

    // Registration
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexPage(final Locale locale) {
        return "redirect:/loginForm.html?lang=" + locale.getLanguage();
    }


    @RequestMapping(value = "/user/registrationCaptcha", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse captchaRegisterUserAccount(final UserDto accountDto, final HttpServletRequest request, final Locale locale) {
        final String response = request.getParameter("g-recaptcha-response");
        captchaService.processResponse(response);

        accountDto.setUsing2FA(true);

        ResponseEntity<String> responseEntity = null;
        try {
            accountDto.setLogin(accountDto.getEmail());
            responseEntity = ldapService.getEmailByLogin(accountDto.getEmail());
            if (!responseEntity.getStatusCode().toString().equals("200")) {
                throw new IllegalStateException("bad creds");
            }
        } catch (Exception e) {
            return new GenericResponse("bad creds");
        }

        accountDto.setEmail(responseEntity.getBody());

        LOGGER.debug("Registering user account with information: {}", accountDto);

        final User registered = userService.registerNewUserAccount(accountDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
        return new GenericResponse("success");
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
