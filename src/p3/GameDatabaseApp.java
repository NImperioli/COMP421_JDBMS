import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
            String url = "jdbc:db2://winter2025-comp421.cs.mcgill.ca:50000/comp421";
            String userid = "cs421g99";
            String password = "c03p42L-99";

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
                        manageFriendship();
                        break;
                    case 8:
                        showPurchaseHistory();
                        break;
                    case 9:
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
                if (statement != null)
                    statement.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Print the main menu
    private static void printMainMenu() {
        System.out.println("\nGame Database");
        System.out.println("1. Lookup Games by Category");
        System.out.println("2. Add a New User");
        System.out.println("3. Add Game to Wishlist");
        System.out.println("4. View User Wishlist");
        System.out.println("5. Apply Discount to Game");
        System.out.println("6. Buy a Game");
        System.out.println("7. Manage Friendship");
        System.out.println("8. Show Purchase History");
        System.out.println("9. Quit");
        System.out.print("\nPlease Enter Your Option: ");
    }

    // Option 1: Lookup games by category
    private static void lookupGameByCategory() {
        try {
            // List all available categories
            System.out.println("\nAvailable Categories:");
            String listCategoriesQuery = "SELECT cname FROM Category";
            ResultSet categoriesResult = statement.executeQuery(listCategoriesQuery);

            boolean hasCategories = false;
            while (categoriesResult.next()) {
                hasCategories = true;
                String categoryName = categoriesResult.getString("cname");
                System.out.println("- " + categoryName);
            }

            if (!hasCategories) {
                System.out.println("No categories found in the database.");
                return;
            }

            // Prompt the user to enter a category
            System.out.print("\nEnter category name (or type 'exit' to go back): ");
            String category = scanner.nextLine();

            if (category.equalsIgnoreCase("exit")) {
                return; // Exit the function if the user types 'exit'
            }

            // Check if the category exists in the Category table
            String checkCategoryQuery = "SELECT cname FROM Category WHERE cname = '" + category + "'";
            ResultSet categoryResult = statement.executeQuery(checkCategoryQuery);

            if (!categoryResult.next()) {
                // Category does not exist
                System.out.println(
                        "Invalid category: '" + category + "'. Please enter a valid category from the list above.");
                return;
            }

            // Category exists, proceed to find games in this category
            String query = "SELECT g.gid, g.gname, g.price FROM Game g " +
                    "JOIN BelongsTo b ON g.gid = b.gid " +
                    "WHERE b.cname = '" + category + "'";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nGames in category '" + category + "':");
            boolean hasGames = false;
            while (rs.next()) {
                hasGames = true;
                String gameName = rs.getString("gname");
                int gameId = rs.getInt("gid");
                double price = rs.getDouble("price");
                System.out.println("Game: " + gameName + ", Game ID: " + gameId + ", Price: $" + price);
            }

            if (!hasGames) {
                System.out.println("No games found in this category.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 2: Add a new user
    private static void addNewUser() {
        try {
            // Find the maximum uid and increment it by 1
            String maxUidQuery = "SELECT MAX(uid) AS maxUid FROM Users";
            ResultSet rs = statement.executeQuery(maxUidQuery);
            int uid = 1; // Default value if no users exist
            if (rs.next()) {
                uid = rs.getInt("maxUid") + 1;
            }

            System.out.print("Enter user email: ");
            String email = scanner.nextLine();

            System.out.print("Enter user password: ");
            String password = scanner.nextLine();

            // Insert the new user with the generated uid
            String insertSQL = "INSERT INTO Users (uid, uemail, password) VALUES (" +
                    uid + ", '" + email + "', '" + password + "')";
            statement.executeUpdate(insertSQL);
            System.out.println("User added successfully! User ID: " + uid);
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 3: Add game to wishlist
    private static void addGameToWishlist() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Enter game name or game ID: ");
            String gameInput = scanner.nextLine();

            int gid = -1;
            String gameName = null;

            // Check if the input is a number (game ID)
            if (gameInput.matches("\\d+")) {
                gid = Integer.parseInt(gameInput);

                // Validate that the game ID exists and retrieve the game name
                String validateGameQuery = "SELECT gid, gname FROM Game WHERE gid = " + gid;
                ResultSet rs = statement.executeQuery(validateGameQuery);

                if (!rs.next()) {
                    System.out.println("Invalid game ID: " + gid + ". Please enter a valid game ID or name.");
                    return;
                }

                gameName = rs.getString("gname");
            } else {
                // Input is a game name, look up the game ID and name
                String lookupGameQuery = "SELECT gid, gname FROM Game WHERE gname = '" + gameInput + "'";
                ResultSet rs = statement.executeQuery(lookupGameQuery);

                if (!rs.next()) {
                    System.out.println("Invalid game name: '" + gameInput + "'. Please enter a valid game name or ID.");
                    return;
                }

                gid = rs.getInt("gid");
                gameName = rs.getString("gname");
            }

            // Check if the game is already in the user's wishlist
            String checkWishlistQuery = "SELECT * FROM Wish WHERE uid = " + uid + " AND gid = " + gid;
            ResultSet wishlistResult = statement.executeQuery(checkWishlistQuery);

            if (wishlistResult.next()) {
                System.out.println("Game '" + gameName + "' is already in your wishlist.");
                return;
            }

            // Add the game to the wishlist
            String insertSQL = "INSERT INTO Wish (uid, gid) VALUES (" + uid + ", " + gid + ")";
            statement.executeUpdate(insertSQL);
            System.out.println("Game '" + gameName + "' added to wishlist successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 4: View user wishlist (updated to show discounted prices)
    private static void viewUserWishlist() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine();

            // Query to get games in the user's wishlist with current price (including
            // discounts)
            String query = "SELECT g.gid, g.gname, g.price, COALESCE(d.percentOff, 0) AS discount " +
                    "FROM Game g " +
                    "JOIN Wish w ON g.gid = w.gid " +
                    "LEFT JOIN Discount d ON g.gid = d.gid AND CURRENT_DATE BETWEEN d.startDate AND d.endDate " +
                    "WHERE w.uid = " + uid;
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nWishlist for user " + uid + ":");
            boolean hasWishlist = false;
            while (rs.next()) {
                hasWishlist = true;
                String gameName = rs.getString("gname");
                int gameId = rs.getInt("gid");
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount");
                double currentPrice = price * (1 - discount / 100); // Calculate current price

                // Print only the current price
                System.out.println(gameName + ", " + gameId + ", $" + currentPrice);
            }

            if (!hasWishlist) {
                System.out.println("No games in wishlist.");
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

            // Generate a new transaction ID
            String maxTidQuery = "SELECT MAX(tid) AS maxTid FROM Transactions";
            ResultSet rs = statement.executeQuery(maxTidQuery);
            int tid = 1;
            if (rs.next()) {
                tid = rs.getInt("maxTid") + 1;
            }

            // Get the game price and apply any ongoing discount
            String priceQuery = "SELECT g.price, COALESCE(d.percentOff, 0) AS discount " +
                    "FROM Game g " +
                    "LEFT JOIN Discount d ON g.gid = d.gid AND CURRENT_DATE BETWEEN d.startDate AND d.endDate " +
                    "WHERE g.gid = " + gid;
            rs = statement.executeQuery(priceQuery);
            double price = 0;
            if (rs.next()) {
                double originalPrice = rs.getDouble("price");
                double discount = rs.getDouble("discount");
                price = originalPrice * (1 - discount / 100);
            }

            // Insert into Transactions table with status
            String insertTransactionSQL = "INSERT INTO Transactions (tid, date, amount, status, uid) VALUES (" +
                    tid + ", CURRENT_DATE, " + price + ", 'Completed', " + uid + ")";
            statement.executeUpdate(insertTransactionSQL);

            // Check if the user already owns the game
            String ownershipQuery = "SELECT * FROM Own WHERE uid = " + uid + " AND gid = " + gid;
            rs = statement.executeQuery(ownershipQuery);

            if (rs.next()) {
                // Update transaction status to 'Failed'
                String updateTransactionSQL = "UPDATE Transactions SET status = 'Failed' WHERE tid = " + tid;
                statement.executeUpdate(updateTransactionSQL);

                System.out.println("Error: User already owns this game. Transaction ID: " + tid + " marked as Failed.");
                return;
            }

            // Check if the game is in the user's wishlist
            String wishlistQuery = "SELECT * FROM Wish WHERE uid = " + uid + " AND gid = " + gid;
            rs = statement.executeQuery(wishlistQuery);
            boolean wasInWishlist = rs.next(); // True if the game is in the wishlist

            // Insert into Buy table
            String buySQL = "INSERT INTO Buy (uid, gid, tid) VALUES (" +
                    uid + ", " + gid + ", " + tid + ")";
            statement.executeUpdate(buySQL);

            // Add to Own table
            String ownSQL = "INSERT INTO Own (uid, gid) VALUES (" +
                    uid + ", " + gid + ")";
            statement.executeUpdate(ownSQL);

            // Remove from Wishlist if the game was in the wishlist
            if (wasInWishlist) {
                String removeWishlistSQL = "DELETE FROM Wish WHERE uid = " + uid + " AND gid = " + gid;
                statement.executeUpdate(removeWishlistSQL);
            }

            // Update transaction status to 'Completed'
            String updateTransactionSQL = "UPDATE Transactions SET status = 'Completed' WHERE tid = " + tid;
            statement.executeUpdate(updateTransactionSQL);

            System.out.println("Game purchased successfully! Transaction ID: " + tid + ", Price Paid: $" + price);
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 7: manage friends
    private static void manageFriendship() {
        while (true) {
            System.out.println("\nManage Friendship");
            System.out.println("1. Add a Friend");
            System.out.println("2. Remove a Friend");
            System.out.println("3. Back to Main Menu");
            System.out.print("Please Enter Your Option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addFriend();
                    break;
                case 2:
                    removeFriend();
                    break;
                case 3:
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // addFriend method
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
                    uid1 + ", " + uid2 + ", 'Accepted')";
            statement.executeUpdate(insertSQL);
            System.out.println("Friend request sent successfully!");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // removeFriend method
    private static void removeFriend() {
        try {
            System.out.print("Enter your user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Query to fetch all friends of the user
            String friendQuery = "SELECT uid1, uid2 FROM Friend WHERE (uid1 = " + uid + " OR uid2 = " + uid
                    + ") AND status = 'Accepted'";
            ResultSet rs = statement.executeQuery(friendQuery);

            // List to store friend IDs
            java.util.List<Integer> friendIds = new java.util.ArrayList<>();

            System.out.println("\nYour Friends:");
            while (rs.next()) {
                int uid1 = rs.getInt("uid1");
                int uid2 = rs.getInt("uid2");

                // Determine which ID is the friend's ID
                int friendId = (uid1 == uid) ? uid2 : uid1;
                friendIds.add(friendId);
                System.out.println("Friend ID: " + friendId);
            }

            if (friendIds.isEmpty()) {
                System.out.println("You have no friends to remove.");
                return;
            }

            // Prompt the user to select a friend to remove
            System.out.print("Enter the ID of the friend you want to remove: ");
            int friendToRemove = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Check if the entered ID is valid
            if (!friendIds.contains(friendToRemove)) {
                System.out.println("Invalid friend ID. Please try again.");
                return;
            }

            // Ensure uid1 < uid2 for the query
            int uid1 = Math.min(uid, friendToRemove);
            int uid2 = Math.max(uid, friendToRemove);

            // Delete the friendship from the Friend table
            String deleteSQL = "DELETE FROM Friend WHERE uid1 = " + uid1 + " AND uid2 = " + uid2;
            int rowsAffected = statement.executeUpdate(deleteSQL);

            if (rowsAffected > 0) {
                System.out.println("Friend removed successfully!");
            } else {
                System.out.println("Error: Friend relationship not found.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Option 8 purchase history
    private static void showPurchaseHistory() {
        try {
            System.out.print("Enter user ID: ");
            int uid = scanner.nextInt();
            scanner.nextLine();

            // Query to retrieve all transactions for the user
            String query = "SELECT t.tid, t.date, t.amount, t.status, g.gname " +
                    "FROM Transactions t " +
                    "LEFT JOIN Buy b ON t.tid = b.tid " +
                    "LEFT JOIN Game g ON b.gid = g.gid " +
                    "WHERE t.uid = " + uid;
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nPurchase history for user " + uid + ":");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                int tid = rs.getInt("tid");
                String date = rs.getString("date");
                double amount = rs.getDouble("amount");
                String status = rs.getString("status");
                String gameName = rs.getString("gname");

                System.out.println("Transaction ID: " + tid + ", Date: " + date + ", Amount: $" + amount +
                        ", Status: " + status + ", Game: " + (gameName != null ? gameName : "N/A"));
            }

            if (!hasTransactions) {
                System.out.println("No transactions found for this user.");
            }
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