package com.example.dao;

import com.example.model.Customer;
import com.example.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private Connection connection;

    public CustomerDAO() {
        connection = DBUtil.getConnection();
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers");
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Get customer by field
    public List<Customer> getFilterCustomer(String field, String searchText) {
        List<Customer> customers = new ArrayList<>();
        try {
            String sql = "";
            switch (field) {
                case "customer_id":
                    sql = "SELECT * FROM customers WHERE customer_id=?";
                    break;
                case "name":
                    sql = "SELECT * FROM customers WHERE name LIKE CONCAT('%', ?, '%')";
                    break;
                case "email":
                    sql = "SELECT * FROM customers WHERE email LIKE CONCAT('%', ?, '%')";
                    break;
                case "phone":
                    sql = "SELECT * FROM customers WHERE phone LIKE CONCAT('%', ?, '%')";
                    break;
                case "address":
                    sql = "SELECT * FROM customers WHERE address LIKE CONCAT('%', ?, '%')";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + field);
            }
            
            PreparedStatement ps = connection.prepareStatement(sql);
            if (field.equals("customer_id")) {
                ps.setInt(1, Integer.parseInt(searchText));
            } else {
                ps.setString(1, searchText);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Insert new customer
    public void addCustomer(Customer customer) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)");
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update customer
    public void updateCustomer(Customer customer) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE customers SET name=?, email=?, phone=?, address=? WHERE customer_id=?");
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete customer
    public void deleteCustomer(int customerId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM customers WHERE customer_id=?");
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get customer by ID
    public Customer getCustomerById(int customerId) {
        Customer customer = new Customer();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customers WHERE customer_id=?");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setAddress(rs.getString("address"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }
}