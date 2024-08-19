package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.ParseUtil;

public class Movie {
  private String posterLink;
  private String seriesTitle;
  private int releasedYear;
  private String certificate;
  private String runtime;
  private String[] genre;
  private float imdbRating;
  private String overview;
  private int metaScore;
  private String director;
  private String star1;
  private String star2;
  private String star3;
  private String star4;
  private int numberOfVotes;
  private int gross;

  public Movie() { }

  public Movie(String posterLink, String seriesTitle, int releasedYear, String certificate, String runtime, String[] genre, float imdbRating, String overview, int metaScore, String director, String star1, String star2, String star3, String star4, int numberOfVotes, int gross) {
    this.posterLink = posterLink;
    this.seriesTitle = seriesTitle;
    this.releasedYear = releasedYear;
    this.certificate = certificate;
    this.runtime = runtime;
    this.genre = genre;
    this.imdbRating = imdbRating;
    this.overview = overview;
    this.metaScore = metaScore;
    this.director = director;
    this.star1 = star1;
    this.star2 = star2;
    this.star3 = star3;
    this.star4 = star4;
    this.numberOfVotes = numberOfVotes;
    this.gross = gross;
  }

  public Movie(String line) {
    String[] fields = ParseUtil.parseCSVLine(line);

    this.posterLink = fields[0];
    this.seriesTitle = fields[1];
    this.releasedYear = ParseUtil.parseInt(fields[2]);
    this.certificate = fields[3];
    this.runtime = fields[4];
    this.genre = fields[5].split(",");
    this.imdbRating = Float.parseFloat(fields[6]);
    this.overview = fields[7];
    this.metaScore = ParseUtil.parseInt(fields[8]);
    this.director = fields[9];
    this.star1 = fields[10];
    this.star2 = fields[11];
    this.star3 = fields[12];
    this.star4 = fields[13];
    this.numberOfVotes = ParseUtil.parseInt(fields[14]);
    this.gross = ParseUtil.parseInt(fields[15]);
  }

  @Override
  public String toString() {
    return seriesTitle
        + " (" + releasedYear + ")"
        + " - " + director;
  }
}
