package com.example.dao;

import com.example.model.Employee;
import com.example.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private Connection connection;

    public EmployeeDAO() {
        connection = DBUtil.getConnection();
    }

    // Get all employees
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM employees");
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setName(rs.getString("name"));
                employee.setGender(rs.getString("gender"));
                employee.setPosition(rs.getString("position"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // Get employees by filter
    public List<Employee> getFilteredEmployees(String field, String searchText) {
        List<Employee> employees = new ArrayList<>();
        try {
            String sql = "";
            switch (field) {
                case "employee_id":
                    sql = "SELECT * FROM employees WHERE employee_id=?";
                    break;
                case "name":
                    sql = "SELECT * FROM employees WHERE name LIKE CONCAT('%', ?, '%')";
                    break;
                case "position":
                    sql = "SELECT * FROM employees WHERE position LIKE CONCAT('%', ?, '%')";
                    break;
                case "email":
                    sql = "SELECT * FROM employees WHERE email LIKE CONCAT('%', ?, '%')";
                    break;
                case "phone":
                    sql = "SELECT * FROM employees WHERE phone LIKE CONCAT('%', ?, '%')";
                    break;
                case "gender":
                    sql = "SELECT * FROM employees WHERE gender LIKE CONCAT('%', ?, '%')";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + field);
            }

            PreparedStatement ps = connection.prepareStatement(sql);
            if (field.equals("employeeId")) {
                ps.setInt(1, Integer.parseInt(searchText));
            } else {
                ps.setString(1, searchText);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setName(rs.getString("name"));
                employee.setGender(rs.getString("gender"));
                employee.setPosition(rs.getString("position"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // Insert new employee
    public void addEmployee(Employee employee) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO employees (name, gender, position, email, phone) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, employee.getName());
            ps.setString(2, employee.getGender());
            ps.setString(3, employee.getPosition());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update employee
    public void updateEmployee(Employee employee) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE employees SET name=?, gender=?, position=?, email=?, phone=? WHERE employee_id=?");
            ps.setString(1, employee.getName());
            ps.setString(2, employee.getGender());
            ps.setString(3, employee.getPosition());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.setInt(6, employee.getEmployeeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete employee
    public void deleteEmployee(int employeeId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM employees WHERE employee_id=?");
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get employee by ID
    public Employee getEmployeeById(int employeeId) {
        Employee employee = new Employee();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM employees WHERE employee_id=?");
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setName(rs.getString("name"));
                employee.setName(rs.getString("gender"));
                employee.setPosition(rs.getString("position"));
                employee.setEmail(rs.getString("email"));
                employee.setPhone(rs.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employee;
    }
}
