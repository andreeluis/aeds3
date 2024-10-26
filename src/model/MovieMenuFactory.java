package model;

import java.util.Scanner;

import model.interfaces.MenuFactory;
import util.ParseUtil;

public class MovieMenuFactory implements MenuFactory<Movie> {
  @Override
  public String getEntityName() {
    return "Filme";
  }

  @Override
  public int searchRegister(Scanner scanner) {
    System.out.println("Como você quer buscar pelo filme? ");
    System.out.println(" 1 - Por ID");
    System.out.println(" 2 - Por título");
    System.out.println(" 3 - Por descrição");
    System.out.println(" 4 - Por título e descrição");
    System.out.print("Selecione a opção desejada: ");

    return scanner.nextInt();
  }

  @Override
  public Movie createRegister(Scanner scanner) {
    Movie movie = new Movie();

    return this.editRegister(scanner, movie);
  }

  @Override
  public Movie editRegister(Scanner scanner, Movie movie) {
    System.out.print(" - Nome do filme: ");
    movie.setTitle(scanner.nextLine());

    System.out.print(" - Resumo: ");
    movie.setMovieInfo(scanner.nextLine());

    System.out.print(" - Ano de lançamento: ");
    movie.setYear(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - Distribuidor: ");
    movie.setDistributor(scanner.nextLine());

    System.out.print(" - Orçamento: ");
    movie.setBudget(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - Domestic Opening: ");
    movie.setDomesticOpening(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - Domestic Sales: ");
    movie.setDomesticSales(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - International Sales: ");
    movie.setInternationalSales(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - World Wide Sales: ");
    movie.setWorldWideSales(ParseUtil.parseInt(scanner.nextLine()));

    System.out.print(" - Data de lançamento: ");
    movie.setReleaseDate(ParseUtil.parseLong(scanner.nextLine()));

    System.out.print(" - Gênero: ");
    movie.setGenre(scanner.nextLine().split(","));

    System.out.print(" - Duração: ");
    movie.setRunningTime(scanner.nextLine());

    System.out.print(" - Licença: ");
    movie.setLicense(scanner.nextLine());

    return movie;
  }

}
