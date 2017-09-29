
package com.twitter.client.network.response.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Url_ {

    @SerializedName("urls")
    @Expose
    private List<Url__> urls = null;

    public List<Url__> getUrls() {
        return urls;
    }

    public void setUrls(List<Url__> urls) {
        this.urls = urls;
    }

}
