package net.onebean.user.mngt.api.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author 0neBean
 */
public class ResetUserPasswordReq {


    @NotBlank(message = "filed of appId can not be empty")
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }



    @NotBlank(message = "filed of userId can not be empty")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
