package com.example.dao;

import com.example.model.Category;
import com.example.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private Connection connection;

    public CategoryDAO() {
        connection = DBUtil.getConnection();
    }

    // get all categories
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM category");
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Get Category by field 
    public List<Category> getFilterCategory(String field, String searchText) {
        List<Category> categories = new ArrayList<>();
        try {
            String sql = "";
            switch (field) {
                case "category_id":
                    sql = "SELECT * FROM category WHERE category_id=?";
                    break;
                case "name":
                    sql = "SELECT * FROM category WHERE name LIKE CONCAT('%', ?, '%')";
                    break;
                case "description":
                    sql = "SELECT * FROM category WHERE description LIKE CONCAT('%', ?, '%')";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + field);
            }

            PreparedStatement ps = connection.prepareStatement(sql);
            if (field.equals("category_id")) {
                ps.setInt(1, Integer.parseInt(searchText));
            } else {
                ps.setString(1, searchText);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // insert new category
    public void addCategory(Category category) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO category (name, description) VALUES (?,?)");
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // update category
    public void updateCategory(Category category) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE category SET name=?, description=? WHERE category_id=?");
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete category
    public void deleteCategory(int categoryId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM category WHERE category_id=?");
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // get category by id
    public Category getCategoryById(int categoryId) {
        Category category = new Category();
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM category WHERE category_id=?");
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    // Get all laptops in a specific category
    public List<String> getProductByCategoryId(int categoryId) {
        List<String> laptops = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT model FROM products WHERE category_id=?");
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                laptops.add(rs.getString("model"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return laptops;
    }
}