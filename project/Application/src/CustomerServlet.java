import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM customer").executeQuery();

            JsonArrayBuilder allCustomers = Json.createArrayBuilder();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer.add("id", id);
                customer.add("name", name);
                customer.add("address", address);
                allCustomers.add(customer);
            }
            resp.setContentType("application/json");
            resp.getWriter().write(allCustomers.build().toString());
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String address = req.getParameter("address");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");

            String insertQuery = "INSERT INTO customer (id, name, address) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, address);

            int rowsAffected = preparedStatement.executeUpdate();
            resp.setContentType("text/plain");
            if (rowsAffected > 0) {
                resp.getWriter().write("Customer saved successfully.");
            } else {
                resp.getWriter().write("Customer not saved.");
            }
            preparedStatement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read the JSON body from the request
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            stringBuilder.append(line);
        }
        String requestBody = stringBuilder.toString();

        // Parse the request body as JSON
        JsonObject jsonRequest = Json.createReader(new StringReader(requestBody)).readObject();

        // Extract the data from the JSON
        String id = jsonRequest.getString("id");
        String name = jsonRequest.getString("name");
        String address = jsonRequest.getString("address");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");

            String updateQuery = "UPDATE customer SET name = ?, address = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, address);
            preparedStatement.setString(3, id);

            int rowsAffected = preparedStatement.executeUpdate();
            resp.setContentType("text/plain");
            if (rowsAffected > 0) {
                resp.getWriter().write("Customer updated successfully.");
            } else {
                resp.getWriter().write("Customer not found or not updated.");
            }
            preparedStatement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");

            String deleteQuery = "DELETE FROM customer WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setString(1, id);

            int rowsAffected = preparedStatement.executeUpdate();
            resp.setContentType("text/plain");
            if (rowsAffected > 0) {
                resp.getWriter().write("Customer deleted successfully.");
            } else {
                resp.getWriter().write("Customer not found.");
            }
            preparedStatement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
