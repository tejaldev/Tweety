
package com.twitter.client.network.response.models;

import java.util.List;

import org.parceler.Parcel;

@Parcel
public class VideoInfo {

    //@SerializedName("aspect_ratio")
    private List<Long> aspectRatio = null;

    //@SerializedName("duration_millis")
    private Long durationMillis;

    //@SerializedName("variants")
    private List<Variant> variants = null;

    public List<Long> getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(List<Long> aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

}
