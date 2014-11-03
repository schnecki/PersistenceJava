package persistence;

import java.sql.*;
import javax.sql.*;
import java.util.List;
import java.util.ArrayList;


public class DBConnection {
    private static String dbtime;
    private static final String dbUrl = "jdbc:mysql://localhost/betPrediction";
    private static final String user = "betPrediction";
    private static final String pass = "betPrediction";
    private static final String dbClass = "com.mysql.jdbc.Driver";

    private static final DBConnection instance = new DBConnection();

    private DBConnection() {}

    public static DBConnection getInstance() {
        return instance;
    }

    /** Test if the database connection can be opened.
     * Returns: True if database is accessable.
     */
    public static boolean testDBConnectivity() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(dbUrl, user, pass);
            return true;
        } catch (SQLException ignore)  {
            System.out.println(ignore.toString());
        } finally {
            try {
            if (con != null)
                con.close();
            } catch (SQLException ignore) {}
        }
        return false;
    }

    /** Create a select statement.
     *
     *  Returns the ResultSet, or null in case of a failure.
     */
    public static Object[] selectFirst(String query){
        Connection con = null;
        ResultSet rs = null;
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, user, pass);
            Statement stmt = con.createStatement();
            rs =  stmt.executeQuery(query);

            if (rs != null) {
                if (rs.next()) {
                    ResultSetMetaData metadata = rs.getMetaData();
                    int cols = metadata.getColumnCount();
                    Object[] array = new Object[cols];

                    int j = 1;
                    for (int i = 0; i < cols; i++) {
                        array[i] = rs.getObject(j++);
                    }

                    return array;
                }
            }


            // while (rs.next()) {
            //     // TODO
            //     dbtime = rs.getString(1);
            //     System.out.println(dbtime);
            // } //end while

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (con != null)
                    con.close();

            } catch (SQLException ignore){}
        }
        return null;

    }  //end select


    /** Perform an update or insert statement.
     *
     *  Return true for success.
     */
    public static boolean update(String query) {
        Connection con = null;
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, user, pass);
            Statement stmt = con.createStatement();
            // int updateCount =
            stmt.executeUpdate(query);
            return true;
        } //end try
        catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException ignore){}
        }
        return false;
    }

}  //end class
