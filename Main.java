

import java.lang.Math;
import exceptions.*;
import persistence.*;
import java.util.Date;
import java.io.IOException;


/**
 * Run wit: java -cp ./classes:./lib/* Main
 *
 * @author <a href="mailto:manuel.schneckenreither@student.uibk.ac.at">Manuel Schneckenreither</a>
 * @version 1.0
 */
public class Main {

  /**
   * This is the entry point of the Program.
   *
   * @param args -  will be ignored.
   * @return void
   */
  public static void main(String[] args) {

    // Test DB connection
    if (!DBConnection.testDBConnectivity()) {
      System.out.println("The database server seems to be closed or " +
        "database library not referenced.");
      return;
    }

    try {

      DBConnection.test();

      Country aut = new Country("Autria");
      City ibk = new City("Innsbruck", aut);
      try {
        ibk.save();
        
        ibk.load();
        
      } catch (DBException ex) {
        ex.printStackTrace();
      }


    }

  }
