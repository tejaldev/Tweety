
package com.twitter.client.network.response.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Small {

    @SerializedName("w")
    @Expose
    private int w;
    @SerializedName("h")
    @Expose
    private int h;
    @SerializedName("resize")
    @Expose
    private String resize;

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getResize() {
        return resize;
    }

    public void setResize(String resize) {
        this.resize = resize;
    }

}
