package org.aditya;

import java.sql.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection localConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/e_commerce", "root", "root");

            Connection remoteConnection = DriverManager.getConnection("jdbc:mysql://34.100.251.15:3306/e_commerce", "root", "root");


            // Task 2.1 Fetch item details from the remote database.
            Statement remoteStatement = remoteConnection.createStatement();
            remoteStatement.execute("SET profiling = 1;");
            ResultSet resultSet1 = remoteStatement.executeQuery("SELECT * from inventory where item_id=56");
            resultSet1.next();
            int item_id = resultSet1.getInt("item_id");
            String item_name = resultSet1.getString("item_name");
            int available_quantity = resultSet1.getInt("available_quantity");
            System.out.println("------Item Details from Remote Database------");
            System.out.println("Item ID: "+item_id);
            System.out.println("Item Name: "+item_name);
            System.out.println("Item Quantity: "+available_quantity);


            ResultSet resultSet2 = remoteStatement.executeQuery("SHOW PROFILES;");
            resultSet2.next();
            String query_id = resultSet2.getString("query_id");
            double duration = resultSet2.getDouble("duration");
            String query = resultSet2.getString("query");
            System.out.println("------Profile------");
            System.out.println("query_id: "+query_id);
            System.out.println("duration: "+duration);
            System.out.println("query: "+query);

            // Task 2.2 Create an order in local database
            int order_quantity =1;
            if(order_quantity<=available_quantity) {
                Statement localStatement = localConnection.createStatement();
                localStatement.execute("SET profiling = 1;");
                PreparedStatement preparedStatement = localConnection.prepareStatement("INSERT INTO order_info values(?,?,?,?,?)");
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, 1);
                preparedStatement.setString(3, item_name);
                preparedStatement.setInt(4, order_quantity);
                preparedStatement.setString(5, LocalDate.now().toString());
                int count = preparedStatement.executeUpdate();
                System.out.println("\n------" + count + " Order created in Local Database with quantity "+order_quantity+"------");
                ResultSet resultSet3 = localStatement.executeQuery("SHOW PROFILES;");
                resultSet3.next();
                query_id = resultSet3.getString("query_id");
                duration = resultSet3.getDouble("duration");
                query = resultSet3.getString("query");
                System.out.println("------Profile------");
                System.out.println("query_id: "+query_id);
                System.out.println("duration: "+duration);
                System.out.println("query: "+query);
            }
            else {
                System.out.println("ERR: Can't Place order. Stock is lower than your requested order quantity!");
            }

            // Task 2.3 Write the updated quantity back to the remote database upon order creation
            PreparedStatement preparedStatement = remoteConnection.prepareStatement("UPDATE inventory SET available_quantity=? WHERE item_id=?");
            preparedStatement.setInt(2, item_id);
            preparedStatement.setInt(1, available_quantity - order_quantity);
            int count = preparedStatement.executeUpdate();
            System.out.println("\n------Item Quantity updated in Remote Database------");
            System.out.println("Old Item Quantity: "+available_quantity);
            System.out.println("New Item Quantity: "+(available_quantity - order_quantity));
            ResultSet resultSet4 = remoteStatement.executeQuery("SHOW PROFILES;");
            resultSet4.next();
            resultSet4.next();
            query_id = resultSet4.getString("query_id");
            duration = resultSet4.getDouble("duration");
            query = resultSet4.getString("query");
            System.out.println("------Profile------");
            System.out.println("query_id: "+query_id);
            System.out.println("duration: "+duration);
            System.out.println("query: "+query);


            // Close Both Connections
            localConnection.close();
            remoteConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}