package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 14:06
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class WSMobileUserData implements Serializable {
    @Expose
    @SerializedName("workshopUsers")
    private List<WorkShopMobileUser> workshopUsers;
    @Expose
    @SerializedName("paginator")
    private Paginator paginator;

    public List<WorkShopMobileUser> getWorkshopUsers() {
        if (workshopUsers == null) {
            return new ArrayList<>();
        }
        return workshopUsers;
    }

    public void setWorkshopUsers(List<WorkShopMobileUser> workshopUsers) {
        this.workshopUsers = workshopUsers;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}
