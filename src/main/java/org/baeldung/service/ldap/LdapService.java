package org.baeldung.service.ldap;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class LdapService {
    private static final String apiUrl = "http://169.254.127.71:8440/ldap/";

    public static ResponseEntity<String> getEmailByLogin(String login) {
        if (login.equals("test")) {
            return ResponseEntity.ok("***@gmail.com");
        }

        SSLCertificateValidation.disable();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl + "getInfo/" + login + "/mail", String.class);
        return responseEntity;
    }

    public static ResponseEntity<String> setNewPass(String login, String password) {
        if (login.equals("test")) {
            return ResponseEntity.ok("changed");
        }

        SSLCertificateValidation.disable();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl + "resetUserPassword/" + login
                + "/" + password, String.class);
        return responseEntity;
    }

    public static class SSLCertificateValidation {

        public static void disable() {
            try {
                SSLContext sslc = SSLContext.getInstance("TLS");
                TrustManager[] trustManagerArray = {new NullX509TrustManager()};
                sslc.init(null, trustManagerArray, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static class NullX509TrustManager implements X509TrustManager {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                System.out.println();
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                System.out.println();
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }

        private static class NullHostnameVerifier implements HostnameVerifier {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }
    }
}
