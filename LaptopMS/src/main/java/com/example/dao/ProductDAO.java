package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Product;
import com.example.util.DBUtil;

public class ProductDAO {
    private Connection connection;

    public ProductDAO() {
        connection = DBUtil.getConnection();
    }

    // Add product
    public void addProduct(Product product) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO products (brand_id, category_id, price, model, description, details, image) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, product.getBrand().getBrandId());
            ps.setInt(2, product.getCategory().getCategoryId());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getModel());
            ps.setString(5, product.getDescription());
            ps.setString(6, product.getDetails());
            ps.setString(7, product.getImage());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM productview");
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("productId"));
                product.setModel(rs.getString("model"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setDescription(rs.getString("description"));
                product.setDetails(rs.getString("details"));
                product.setImage(rs.getString("image"));
                product.getBrand().setName(rs.getString("brand"));
                product.getCategory().setName(rs.getString("category"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Update product
    public void updateProduct(Product product) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE products SET brand_id=?, category_id=?, price=?, model=?, description=?, details=?, image=? WHERE product_id=?");
            ps.setInt(1, product.getBrand().getBrandId());
            ps.setInt(2, product.getCategory().getCategoryId());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getModel());
            ps.setString(5, product.getDescription());
            ps.setString(6, product.getDetails());
            ps.setString(7, product.getImage());
            ps.setInt(8, product.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete product and its sales records
    public void deleteProduct(int productId) {
        try {
            // Step 1: Delete sales for the product
            String deleteSalesSql = "DELETE FROM sales WHERE product_id=?";
            PreparedStatement deleteSalesStmt = connection.prepareStatement(deleteSalesSql);
            deleteSalesStmt.setInt(1, productId);
            deleteSalesStmt.executeUpdate();
            
            // Step 2: Delete product from products table
            String deleteProductSql = "DELETE FROM products WHERE product_id=?";
            PreparedStatement deleteProductStmt = connection.prepareStatement(deleteProductSql);
            deleteProductStmt.setInt(1, productId);
            deleteProductStmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get product by ID
    public Product getProductById(int productId) {
        Product product = new Product();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM products WHERE product_id=?");
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                product.setProductId(rs.getInt("product_id"));
                product.getBrand().setBrandId(rs.getInt("brand_id"));
                product.getCategory().setCategoryId(rs.getInt("category_id"));
                product.setPrice(rs.getDouble("price"));
                product.setModel(rs.getString("model"));
                product.setDescription(rs.getString("description"));
                product.setDetails(rs.getString("details"));
                product.setImage(rs.getString("image"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }
}
