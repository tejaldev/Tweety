
package com.twitter.client.network.response.models;

import org.parceler.Parcel;

@Parcel
public class Variant {

    //@SerializedName("bitrate")
    private Long bitrate;
    //@SerializedName("content_type")
    private String contentType;
    //@SerializedName("url")
    private String url;

    public Long getBitrate() {
        return bitrate;
    }

    public void setBitrate(Long bitrate) {
        this.bitrate = bitrate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
