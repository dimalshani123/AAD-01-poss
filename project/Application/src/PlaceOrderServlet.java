import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;

@WebServlet(urlPatterns = "/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Build request body string
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            stringBuilder.append(line);
        }
        String requestBody = stringBuilder.toString();

// Parse the JSON
        JsonObject jsonRequest = Json.createReader(new StringReader(requestBody)).readObject();

// Extract order details from the request
        String customerId = jsonRequest.getString("customerId");
        String itemId = jsonRequest.getString("itemId");

// Retrieve itemPrice and convert it
        JsonValue itemPriceValue = jsonRequest.get("itemPrice");
        double itemPrice = 0.0;

        if (itemPriceValue.getValueType() == JsonValue.ValueType.STRING) {
            // If itemPrice is a string, parse it to a double
            String itemPriceString = itemPriceValue.toString().replace("\"", "");
            try {
                itemPrice = Double.parseDouble(itemPriceString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid item price: " + itemPriceString);
            }
        } else if (itemPriceValue.getValueType() == JsonValue.ValueType.NUMBER) {
            // If itemPrice is already a number, use it directly
            itemPrice = ((JsonNumber) itemPriceValue).doubleValue();
        }

// Retrieve quantity and ensure it's a valid integer
        JsonValue quantityValue = jsonRequest.get("quantity");
        int quantity = 0;

        if (quantityValue.getValueType() == JsonValue.ValueType.NUMBER) {
            // If quantity is a JsonNumber, use it directly
            quantity = ((JsonNumber) quantityValue).intValue();
        } else if (quantityValue.getValueType() == JsonValue.ValueType.STRING) {
            // If quantity is a JsonString, convert it to an integer
            String quantityString = quantityValue.toString().replace("\"", "");
            try {
                quantity = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid quantity: " + quantityString);
            }
        } else {
            throw new IllegalArgumentException("Invalid quantity type.");
        }



        try {
            // Database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");

            // Calculate total cost
            double totalCost = itemPrice * quantity;

            // Insert the order into the database
            String insertOrderQuery = "INSERT INTO orders (customer_id, item_id, quantity, total_cost) VALUES (?, ?, ?, ?)";
            PreparedStatement orderStatement = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStatement.setString(1, customerId);
            orderStatement.setString(2, itemId);
            orderStatement.setInt(3, quantity);
            orderStatement.setDouble(4, totalCost);

            int rowsAffected = orderStatement.executeUpdate();
            if (rowsAffected > 0) {
                // Get the generated order ID
                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);  // Retrieve generated order ID

                    // Create a JSON response with the orderId
                    JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
                    responseBuilder.add("orderId", orderId);
                    responseBuilder.add("message", "Order placed successfully.");
                    responseBuilder.add("customerId", customerId);
                    responseBuilder.add("itemId", itemId);
                    responseBuilder.add("quantity", quantity);
                    responseBuilder.add("totalCost", totalCost);

                    resp.setContentType("application/json");
                    resp.getWriter().write(responseBuilder.build().toString());
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\": \"Failed to place the order.\"}");
            }

            orderStatement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\": \"Error: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
