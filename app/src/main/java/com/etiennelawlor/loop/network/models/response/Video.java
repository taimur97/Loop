package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Video implements Parcelable {
    // region Member Variables
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("link")
    private String link;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("width")
    private Integer width;
    @SerializedName("language")
    private String language;
    @SerializedName("height")
    private Integer height;
    @SerializedName("embed")
    private Embed embed;
    @SerializedName("created_time")
    private String createdTime;
    @SerializedName("modified_time")
    private String modifiedTime;
    @SerializedName("content_rating")
    private List<String> contentRating;
//    @SerializedName("license")
//    private Object license;
    @SerializedName("pictures")
    private Pictures pictures;
    @SerializedName("tags")
    private List<Tag> tags;
    @SerializedName("stats")
    private Stats stats;
    @SerializedName("metadata")
    private Metadata metadata;
    @SerializedName("user")
    private User user;
//    @SerializedName("app")
//    private Object app;
    @SerializedName("status")
    private String status;
//    @SerializedName("embed_presets")
//    private Object embedPresets;
    // endregion

    // region Getters
    public String getUri() {
        if (TextUtils.isEmpty(uri))
            return "";
        else
            return uri;
    }

    public String getName() {
        if (TextUtils.isEmpty(name))
            return "";
        else
            return name;
    }

    public String getDescription() {
        if (TextUtils.isEmpty(description))
            return "";
        else
            return description;
    }

    public String getLink() {
        if (TextUtils.isEmpty(link))
            return "";
        else
            return link;
    }

    public Integer getDuration() {
        if(duration == null)
            return -1;
        else
            return duration;
    }

    public Integer getWidth() {
        if(width == null)
            return -1;
        else
            return width;
    }

    public String getLanguage() {
        if (TextUtils.isEmpty(language))
            return "";
        else
            return language;
    }

    public Integer getHeight() {
        if(height == null)
            return -1;
        else
            return height;
    }

    public Embed getEmbed() {
        return embed;
    }

    public String getCreatedTime() {
        if (TextUtils.isEmpty(createdTime))
            return "";
        else
            return createdTime;
    }

    public String getModifiedTime() {
        if (TextUtils.isEmpty(modifiedTime))
            return "";
        else
            return modifiedTime;
    }

    public List<String> getContentRating() {
        return contentRating;
    }

//    public Object getLicense() {
//        return license;
//    }

    public Pictures getPictures() {
        return pictures;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Stats getStats() {
        return stats;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public User getUser() {
        return user;
    }

//    public Object getApp() {
//        return app;
//    }

    public String getStatus() {
        return status;
    }

//    public Object getEmbedPresets() {
//        return embedPresets;
//    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setEmbed(Embed embed) {
        this.embed = embed;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setContentRating(List<String> contentRating) {
        this.contentRating = contentRating;
    }

//    public void setLicense(Object license) {
//        this.license = license;
//    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    public void setApp(Object app) {
//        this.app = app;
//    }

    public void setStatus(String status) {
        this.status = status;
    }

//    public void setEmbedPresets(Object embedPresets) {
//        this.embedPresets = embedPresets;
//    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getUri());
        dest.writeString(getName());
        dest.writeString(getDescription());
        dest.writeString(getLink());
        dest.writeInt(getDuration());
        dest.writeInt(getWidth());
        dest.writeString(getLanguage());
        dest.writeInt(getHeight());
        dest.writeParcelable(getEmbed(), flags);
        dest.writeString(getCreatedTime());
        dest.writeString(getModifiedTime());
        dest.writeStringList(getContentRating());
//        dest.writeParcelable(getLicense(), flags);
        dest.writeParcelable(getPictures(), flags);
//        dest.writeParcelable(getTags(), flags);
        dest.writeTypedList(getTags());
        dest.writeParcelable(getStats(), flags);
        dest.writeParcelable(getMetadata(), flags);
        dest.writeParcelable(getUser(), flags);
//        dest.writeParcelable(getApp(), flags);
        dest.writeString(getStatus());
//        dest.writeParcelable(getEmbedPresets(), flags);
    }
    // endregion

    public static final Creator<Video> CREATOR = new Creator<Video>() {

        @Override
        public Video createFromParcel(Parcel source) {
            Video video = new Video();

            video.setUri(source.readString());
            video.setName(source.readString());
            video.setDescription(source.readString());
            video.setLink(source.readString());
            video.setDuration(source.readInt());
            video.setWidth(source.readInt());
            video.setLanguage(source.readString());
            video.setHeight(source.readInt());
            video.setEmbed((Embed) source.readParcelable(Embed.class.getClassLoader()));
            video.setCreatedTime(source.readString());
            video.setModifiedTime(source.readString());

            List<String> contentRating = new ArrayList<String>();
            source.readStringList(contentRating);
            video.setContentRating(contentRating);
//            video.setLicense((Object) source.readParcelable(Object.class.getClassLoader()));
            video.setPictures((Pictures) source.readParcelable(Pictures.class.getClassLoader()));
//            video.setTags((Tags) source.readParcelable(Tags.class.getClassLoader()));

            video.setTags(source.createTypedArrayList(Tag.CREATOR));


            video.setStats((Stats) source.readParcelable(Stats.class.getClassLoader()));
            video.setMetadata((Metadata) source.readParcelable(Metadata.class.getClassLoader()));
            video.setUser((User) source.readParcelable(User.class.getClassLoader()));
//            video.setApp((Object) source.readParcelable(Object.class.getClassLoader()));
            video.setStatus(source.readString());
//            video.setEmbedPresets((Object) source.readParcelable(Object.class.getClassLoader()));

            return video;
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}