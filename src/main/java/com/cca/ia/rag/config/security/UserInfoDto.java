package com.cca.ia.rag.config.security;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author ccsw
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDto {

    private String username;

    private String mail;

    private String displayName;

    private String lastName;

    private String firstName;

    private String saga;

    private String officeName;

    private String grade;

    private Map<String, List<String>> roles;

    private Date expiredDate;

    /**
     * @return the role
     */
    public Map<String, List<String>> getRoles() {
        if (this.roles == null)
            this.roles = new HashMap<>();

        return this.roles;
    }

    /**
     * Recupera los roles de una app
     * @param appCode
     * @return
     */
    public List<String> getAppRoles(String appCode) {

        return getRoles().getOrDefault(appCode, new ArrayList<>());
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the grade
     */
    public String getGrade() {
        return grade;
    }

    /**
     * @param grade the grade to set
     */
    public void setGrade(String grade) {
        this.grade = grade;
    }

    /**
     * @param roles the role to set
     */
    public void setRoles(Map<String, List<String>> roles) {
        this.roles = roles;
    }

    /**
     * @return the saga
     */
    public String getSaga() {
        return saga;
    }

    /**
     * @param saga the saga to set
     */
    public void setSaga(String saga) {
        this.saga = saga;
    }

    /**
     * @return the officeName
     */
    public String getOfficeName() {
        return officeName;
    }

    /**
     * @param officeName the officeName to set
     */
    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    /**
     * @return the expiredDate
     */
    public Date getExpiredDate() {
        return expiredDate;
    }

    /**
     * @param expiredDate the expiredDate to set
     */
    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String appCode) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (String role : this.getAppRoles(appCode)) {
            authorities.add(new GrantedAuthority() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getAuthority() {

                    return role;
                }
            });
        }

        return authorities;

    }
}