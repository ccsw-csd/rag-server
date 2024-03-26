package com.cca.ia.rag.config.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ccsw
 */
@Component
public class JwtService {

    private Map<String, CacheData> userCache = new HashMap<>();

    @Value("${app.sso.url}")
    private String ssoUrl;

    /**
    * Create UserDetails from JWT
    *
    * @param jwtToken The json web token
    * @return userDetails
    */
    public final UserInfoDto createUserDetails(String jwtToken) {

        CacheData data = userCache.get(jwtToken);

        if (data == null) {
            data = getUserFromCacheOrServer(jwtToken);
        }

        return data.getUser();
    }

    private CacheData getUserFromCacheOrServer(String jwtToken) {

        CacheData data;
        UserInfoDto userDetails = getUserFromSSOServer(jwtToken);

        if (userDetails == null)
            data = new CacheData();
        else
            data = new CacheData(userDetails, userDetails.getExpiredDate());

        userCache.put(jwtToken, data);
        return data;
    }

    private UserInfoDto getUserFromSSOServer(String jwtToken) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.postForObject(ssoUrl + "validate", new ValidateRequestDto(jwtToken), UserInfoDto.class);
        } catch (Exception e) {
            return null;
        }
    }


    public Date extractExpiration(String token) {

        UserInfoDto dto = createUserDetails(token);
        return dto.getExpiredDate();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }




    private class ValidateRequestDto {
        private String token;

        public ValidateRequestDto(String token) {
            super();
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    private class CacheData {
        private static final long ONE_DAY = 24 * 60 * 60 * 1000L;

        private UserInfoDto user;
        private Date expiredDate;

        public CacheData() {
            this.user = null;
            this.expiredDate = new Date(System.currentTimeMillis() + ONE_DAY);
        }

        public CacheData(UserInfoDto user, Date expiredDate) {
            this.user = user;
            this.expiredDate = expiredDate;
        }

        /**
         * @return the user
         */
        public UserInfoDto getUser() {
            return user;
        }

        /**
         * @return the expiredDate
         */
        public Date getExpiredDate() {
            return expiredDate;
        }

    }
}