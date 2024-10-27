package model.interfaces;

import java.util.Scanner;

import model.Register;

public interface MenuFactory<T extends Register> {
  String getEntityName();

  /**
   * @param scanner The current object of Scanner.
   * @return The search option selected by the user.
   * <ul>
   *  <li> 1: Search by ID</li>
   *  <li> 2: Search by title</li>
   *  <li> 3: Search by description</li>
   *  <li> 4: Search by title and description</li>
   * </ul>
   */
  int searchRegister(Scanner scanner);
  T createRegister(Scanner scanner);
  T editRegister(Scanner scanner, T register);
}
