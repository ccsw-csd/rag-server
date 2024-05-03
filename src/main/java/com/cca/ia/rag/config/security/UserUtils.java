package com.cca.ia.rag.config.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtils {

    /**
     * @return UserDetailsJWTDto
     */
    public static UserInfoDto getUserDetails() {

        return (UserInfoDto) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }

    public static boolean isGranted(String appCode, String roleGranted) {
        for (String role : getUserDetails().getAppRoles(appCode)) {
            if (role.equals(roleGranted)) {
                return true;
            }
        }

        return false;
    }

}
