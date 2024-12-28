import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

@WebServlet(urlPatterns = "/customer")
    public class CustomerServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            //ESTABLISH DATABASE CONNECTION
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("fine");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/poss", "root", "Ijse@123");
                ResultSet resultSet = connection.prepareStatement("select * from customer").executeQuery();

                //create JSON array
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
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String address = req.getParameter("address");

        try {
            Class.forName("com.mysql.jdbc.Driver");

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

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    }

