package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import util.ParseUtil;

public class Movie {
  private int id;
  private String title;
  private String movieInfo;
  private int year;
  private String distributor;
  private int budget;
  private int domesticOpening;
  private int domesticSales;
  private int internationalSales;
  private int worldWideSales;
  private String releaseDate;
  private String[] genre;
  private String runningTime;
  private String license;
  private static int lastId = -1;

  // id
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  // title
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    if (!title.isBlank()) {
      this.title = title;
    } else if (this.title == null || this.title.isBlank()) {
      this.title = "Unknown";
    }
  }

  // movieInfo
  public String getMovieInfo() {
    return movieInfo;
  }

  public void setMovieInfo(String movieInfo) {
    if (!movieInfo.isBlank()) {
      this.movieInfo = movieInfo;
    } else if (this.movieInfo == null || this.movieInfo.isBlank()) {
      this.movieInfo = "Unknown";
    }
  }

  // year
  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    if (year >= 0) {
      this.year = year;
    }
  }

  // distributor
  public String getDistributor() {
    return distributor;
  }

  public void setDistributor(String distributor) {
    if (!distributor.isBlank()) {
      this.distributor = distributor;
    } else if (this.distributor == null || this.distributor.isBlank()) {
      this.distributor = "Unknown";
    }
  }

  // budget
  public int getBudget() {
    return budget;
  }

  public void setBudget(int budget) {
    if (budget >= 0) {
      this.budget = budget;
    }
  }

  // domesticOpening
  public int getDomesticOpening() {
    return domesticOpening;
  }

  public void setDomesticOpening(int domesticOpening) {
    if (domesticOpening >= 0) {
      this.domesticOpening = domesticOpening;
    }
  }

  // domesticSales
  public int getDomesticSales() {
    return domesticSales;
  }

  public void setDomesticSales(int domesticSales) {
    if (domesticSales >= 0) {
      this.domesticSales = domesticSales;
    }
  }

  // internationalSales
  public int getInternationalSales() {
    return internationalSales;
  }

  public void setInternationalSales(int internationalSales) {
    if (internationalSales >= 0) {
      this.internationalSales = internationalSales;
    }
  }

  // worldWideSales
  public int getWorldWideSales() {
    return worldWideSales;
  }

  public void setWorldWideSales(int worldWideSales) {
    if (worldWideSales >= 0) {
      this.worldWideSales = worldWideSales;
    }
  }

  // releaseDate
  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    if (!releaseDate.isBlank()) {
      this.releaseDate = releaseDate;
    } else if (this.releaseDate == null || this.releaseDate.isBlank()) {
      this.releaseDate = "Unknown";
    }
  }

  // genre
  public String[] getGenre() {
    return genre;
  }

  public void setGenre(String[] genre) {
    this.genre = genre;
  }

  // runningTime
  public String getRunningTime() {
    return runningTime;
  }

  public void setRunningTime(String runningTime) {
    if (!runningTime.isBlank()) {
      this.runningTime = runningTime;
    } else if (this.runningTime == null || this.runningTime.isBlank()) {
      this.runningTime = "Unknown";
    }
  }

  // license
  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    if (!license.isBlank()) {
      this.license = license;
    } else if (this.license == null || this.license.isBlank()) {
      this.license = "Unknown";
    }
  }

  public static int getLastId() {
    return lastId;
  }

  public static void setLastId(int lastId) {
    Movie.lastId = lastId;
  }

  public Movie() {
    this.setId(-1);
  }

  public Movie(String title, String movieInfo, int year, String distributor, int budget, int domesticOpening, int domesticSales, int internationalSales, int worldWideSales, String releaseDate, String[] genre, String runningTime, String license) {
    this(); // construtor sem parametro

    this.setTitle(title);
    this.setMovieInfo(movieInfo);
    this.setYear(year);
    this.setDistributor(distributor);
    this.setBudget(budget);
    this.setDomesticOpening(domesticOpening);
    this.setDomesticSales(domesticSales);
    this.setInternationalSales(internationalSales);
    this.setWorldWideSales(worldWideSales);
    this.setReleaseDate(releaseDate);
    this.setGenre(genre);
    this.setRunningTime(runningTime);
    this.setLicense(license);
  }

  public Movie(String line) {
    this(); // construtor sem parametro

    String[] fields = ParseUtil.parseCSVLine(line);
    // fields[0] -> id (não utilizado)

    this.setTitle(fields[1]);
    this.setMovieInfo(fields[2]);
    this.setYear(ParseUtil.parseInt(fields[3]));
    this.setDistributor(fields[4]);
    this.setBudget(ParseUtil.parseInt(fields[5]));
    this.setDomesticOpening(ParseUtil.parseInt(fields[6]));
    this.setDomesticSales(ParseUtil.parseInt(fields[7]));
    this.setInternationalSales(ParseUtil.parseInt(fields[8]));
    this.setWorldWideSales(ParseUtil.parseInt(fields[9]));
    this.setReleaseDate(fields[10]);
    this.setGenre(fields[11].split(","));
    this.setRunningTime(fields[12]);
    this.setLicense(fields[13]);
  }

  public Movie(int id, String title, String movieInfo, int year, String distributor, int budget, int domesticOpening, int domesticSales, int internationalSales, int worldWideSales, String releaseDate, String[] genre,      String runningTime, String license) {
    this.setId(id);
    this.setTitle(title);
    this.setMovieInfo(movieInfo);
    this.setYear(year);
    this.setDistributor(distributor);
    this.setBudget(budget);
    this.setDomesticOpening(domesticOpening);
    this.setDomesticSales(domesticSales);
    this.setInternationalSales(internationalSales);
    this.setWorldWideSales(worldWideSales);
    this.setReleaseDate(releaseDate);
    this.setGenre(genre);
    this.setRunningTime(runningTime);
    this.setLicense(license);
  }

  public Movie(byte[] byteArray) {
    try {
      ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
      DataInputStream data = new DataInputStream(input);

      this.setId(data.readInt());
      this.setTitle(data.readUTF());
      this.setMovieInfo(data.readUTF());
      this.setYear(data.readInt());
      this.setDistributor(data.readUTF());
      this.setBudget(data.readInt());
      this.setDomesticOpening(data.readInt());
      this.setDomesticSales(data.readInt());
      this.setInternationalSales(data.readInt());
      this.setWorldWideSales(data.readInt());
      this.setReleaseDate(data.readUTF());
      //this.setGenre(genre);
      this.setRunningTime(data.readUTF());
      this.setLicense(data.readUTF());
    } catch (IOException e) { }
  }

  @Override
  public String toString() {
    return "[" + this.getId() + "]"
        + " " + this.getTitle()
        + " (" + this.getYear() + ")";
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

    data.writeInt(this.getId());
    data.writeUTF(this.getTitle());
    data.writeUTF(this.getMovieInfo());
    data.writeInt(this.getYear());
    data.writeUTF(this.getDistributor());
    data.writeInt(this.getBudget());
    data.writeInt(this.getDomesticOpening());
    data.writeInt(this.getDomesticSales());
    data.writeInt(this.getInternationalSales());
    data.writeInt(this.getWorldWideSales());
    data.writeUTF(this.getReleaseDate());
    //data.writeUTF(this.getGenre());
    data.writeUTF(this.getRunningTime());
    data.writeUTF(this.getLicense());

    return output.toByteArray();
  }
}
