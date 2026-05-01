import java.sql.*;
import java.util.Scanner;

public class GameDatabaseApp {

    private static Connection con;
    private static Statement statement;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Register the driver
            DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());

            // Connect to the database
            String url = "jdbc:db2://winter2024-comp421.cs.mcgill.ca:50000/comp421";
            String userid = System.getenv("SOCSUSER");
            String password = System.getenv("SOCSPASSWD");

            

            con = DriverManager.getConnection(url, userid, password);
            statement = con.createStatement();

            // Main menu loop
            while (true) {
                printMainMenu();
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        lookupGameByCategory();
                        break;
                    case 2:
                        addNewUser();
                        break;
                    case 3:
                        addGameToWishlist();
                        break;
                    case 4:
                        viewUserWishlist();
                        break;
                    case 5:
                        applyDiscountToGame();
                        break;
                    case 6:
                        buyGame();
                        break;
                    case 7:
                        addFriend();
                        break;
                    case 8:
                        System.out.println("Exiting the application. Goodbye!");
                        statement.close();
                        con.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (statement != null) statement.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Print the main menu
    private static void printMainMenu() {
        System.out.println("\nGame Database Main Menu");
        System.out.println("1. Lookup Games by Category");
        System.out.println("2. Add a New User");
        System.out.println("3. Add Game to Wishlist");
        System.out.println("4. View User Wishlist");
        System.out.println("5. Apply Discount to Game");
        System.out.println("6. Buy a Game");
        System.out.println("7. Add a Friend");
        System.out.println("8. Quit");
        System.out.print("Please Enter Your Option: ");
    }

    // Option 1: Lookup games by category
    private static void lookupGameByCategory() {
        try {
            System.out.print("Enter category name: ");
            String category = scanner.nextLine();

            String query = "SELECT g.gname, g.price FROM Game g " +
                           "JOIN BelongsTo b ON g.gid = b.gid " +
                           "WHERE b.cname = '" + category + "'";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nGames in category '" + category + "':");
            while (rs.next()) {
                String gameName = rs.getString("gname");
                double price = rs.getDouble("price");
                System.out.println("Game: " + gameName + ", Price: $" + price);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 2: Add a new user
    private static void addNewUser() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter user email: ");
            String email = scanner.nextLine();

            System.out.print("Enter user password: ");
            String password = scanner.nextLine();

            String insertSQL = "INSERT INTO Users (uid, uemail, password) VALUES (" +
                               uid + ", '" + email + "', '" + password + "')";
            statement.executeUpdate(insertSQL);
            System.out.println("User added successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 3: Add game to wishlist
    private static void addGameToWishlist() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter game ID: ");
            int gid = scanner.nextInt();
            scanner.nextLine(); 

            String insertSQL = "INSERT INTO Wish (uid, gid) VALUES (" +
                               uid + ", " + gid + ")";
            statement.executeUpdate(insertSQL);
            System.out.println("Game added to wishlist successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 4: View user wishlist
    private static void viewUserWishlist() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); 

            String query = "SELECT g.gname FROM Game g " +
                           "JOIN Wish w ON g.gid = w.gid " +
                           "WHERE w.uid = " + uid;
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nWishlist for user " + uid + ":");
            while (rs.next()) {
                String gameName = rs.getString("gname");
                System.out.println("Game: " + gameName);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 5: Apply discount to game
    private static void applyDiscountToGame() {
        try {
            System.out.print("Enter game ID: ");
            int gid = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter discount percentage: ");
            double percentOff = scanner.nextDouble();
            scanner.nextLine(); 

            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine();

            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = scanner.nextLine();

            // Check for overlapping discounts
            String overlapQuery = "SELECT * FROM Discount WHERE gid = " + gid +
                                 " AND ((startDate <= '" + startDate + "' AND endDate >= '" + startDate + "') OR " +
                                 "(startDate <= '" + endDate + "' AND endDate >= '" + endDate + "'))";
            ResultSet rs = statement.executeQuery(overlapQuery);

            if (rs.next()) {
                System.out.println("Error: Overlapping discount period for this game.");
                return;
            }

            // Insert discount
            String insertSQL = "INSERT INTO Discount (gid, startDate, endDate, percentOff) VALUES (" +
                               gid + ", '" + startDate + "', '" + endDate + "', " + percentOff + ")";
            statement.executeUpdate(insertSQL);
            System.out.println("Discount applied successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 6: Buy a game
    private static void buyGame() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter game ID: ");
            int gid = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter transaction ID: ");
            int tid = scanner.nextInt();
            scanner.nextLine(); 

            // Check if the user already owns the game
            String ownershipQuery = "SELECT * FROM Own WHERE uid = " + uid + " AND gid = " + gid;
            ResultSet rs = statement.executeQuery(ownershipQuery);

            if (rs.next()) {
                System.out.println("Error: User already owns this game.");
                return;
            }

            // Insert into Buy table
            String buySQL = "INSERT INTO Buy (uid, gid, tid) VALUES (" +
                            uid + ", " + gid + ", " + tid + ")";
            statement.executeUpdate(buySQL);

            // Add to Own table
            String ownSQL = "INSERT INTO Own (uid, gid) VALUES (" +
                            uid + ", " + gid + ")";
            statement.executeUpdate(ownSQL);

            System.out.println("Game purchased and added to your library successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 7: Add a friend
    private static void addFriend() {
        try {
            System.out.print("Enter your user ID: ");
            int uid1 = scanner.nextInt();
            scanner.nextLine(); 

            System.out.print("Enter friend's user ID: ");
            int uid2 = scanner.nextInt();
            scanner.nextLine(); 

            // Ensure uid1 < uid2
            if (uid1 > uid2) {
                int temp = uid1;
                uid1 = uid2;
                uid2 = temp;
            }

            // Check if the relationship already exists
            String friendQuery = "SELECT * FROM Friend WHERE uid1 = " + uid1 + " AND uid2 = " + uid2;
            ResultSet rs = statement.executeQuery(friendQuery);

            if (rs.next()) {
                System.out.println("Error: Friend relationship already exists.");
                return;
            }

            // Insert into Friend table
            String insertSQL = "INSERT INTO Friend (uid1, uid2, status) VALUES (" +
                               uid1 + ", " + uid2 + ", 'Pending')";
            statement.executeUpdate(insertSQL);
            System.out.println("Friend request sent successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Handle SQL exceptions
    private static void handleSQLException(SQLException e) {
        System.err.println("SQL Error: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }
}