
package com.twitter.client.network.response.models;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Entities {

    //@SerializedName("media")
    private List<Medium> media = null;

    public List<Medium> getMedia() {
        return media;
    }

    public void setMedia(List<Medium> media) {
        this.media = media;
    }

}
