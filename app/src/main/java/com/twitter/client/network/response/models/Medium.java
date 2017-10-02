
package com.twitter.client.network.response.models;


import org.parceler.Parcel;

@Parcel
public class Medium {

    //@SerializedName("id")
    private long id;

    //@SerializedName("media_url")
    private String mediaUrl;

    //@SerializedName("media_url_https")
    private String mediaUrlHttps;

    //@SerializedName("type")
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaUrlHttps() {
        return mediaUrlHttps;
    }

    public void setMediaUrlHttps(String mediaUrlHttps) {
        this.mediaUrlHttps = mediaUrlHttps;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
