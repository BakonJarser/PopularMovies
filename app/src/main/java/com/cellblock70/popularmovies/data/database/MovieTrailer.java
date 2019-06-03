package com.cellblock70.popularmovies.data.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "trailers",
        foreignKeys = {@ForeignKey(entity = Movie.class, parentColumns = "movie_id",
                childColumns = "movie_id", onDelete = ForeignKey.CASCADE)},
        indices = {@Index("movie_id")})
public class MovieTrailer {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private transient int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "link")
    @SerializedName("key")
    private String link;

    @ColumnInfo(name = "site")
    private String site;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
}
