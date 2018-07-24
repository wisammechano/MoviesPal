
package com.funnyapps.moviespal;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class MovieDetails {

    @Expose
    private Boolean adult;
    @Expose
    private String backdropPath;
    @Expose
    private Object belongsToCollection;
    @Expose
    private Long budget;
    @Expose
    private List<Genre> genres;
    @Expose
    private String homepage;
    @Expose
    private Long id;
    @Expose
    private String imdbId;
    @Expose
    private String originalLanguage;
    @Expose
    private String originalTitle;
    @Expose
    private String overview;
    @Expose
    private Double popularity;
    @Expose
    private String posterPath;
    @Expose
    private List<ProductionCompany> productionCompanies;
    @Expose
    private List<ProductionCountry> productionCountries;
    @Expose
    private String releaseDate;
    @Expose
    private Long revenue;
    @Expose
    private Long runtime;
    @Expose
    private List<SpokenLanguage> spokenLanguages;
    @Expose
    private String status;
    @Expose
    private String tagline;
    @Expose
    private String title;
    @Expose
    private Boolean video;
    @Expose
    private Double voteAverage;
    @Expose
    private Long voteCount;

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Object getBelongsToCollection() {
        return belongsToCollection;
    }

    public void setBelongsToCollection(Object belongsToCollection) {
        this.belongsToCollection = belongsToCollection;
    }

    public Long getBudget() {
        return budget;
    }

    public void setBudget(Long budget) {
        this.budget = budget;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getGenresString(){
        String ret = "";
        for(Genre g :genres) {
            ret += g.getName() + ", ";
        }

        return ret.substring(0, ret.lastIndexOf(","));
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getImdbUrl(){
        return "https://www.imdb.com/title/" + imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public List<ProductionCompany> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionCompany> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public List<ProductionCountry> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(List<ProductionCountry> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseYear() {
        return releaseDate != null ? releaseDate.split("-")[0] : "";
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    public Long getRuntime() {
        return runtime;
    }

    public String getRuntimeString() {
        return runtime.toString() + "mins";
    }

    public void setRuntime(Long runtime) {
        this.runtime = runtime;
    }

    public List<SpokenLanguage> getSpokenLanguages() {
        return spokenLanguages;
    }
    public String getSpokenLanguagesString() {
        String ret = "Languages: ";
        for(SpokenLanguage l :spokenLanguages) {
            ret += l.getName() + ", ";
        }

        return ret.substring(0, ret.lastIndexOf(","));
    }

    public void setSpokenLanguages(List<SpokenLanguage> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public String getRating(){
        return "TMDB Rating: " + voteAverage.toString() + "/10";
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }

}
