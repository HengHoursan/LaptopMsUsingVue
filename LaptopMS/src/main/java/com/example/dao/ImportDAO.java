package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Import;
import com.example.util.DBUtil;

public class ImportDAO {
    private Connection connection;

    public ImportDAO() {
        connection = DBUtil.getConnection();
    }

    // addImport
    public void addImport(Import importItem) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO imports (product_id, quantity, import_date, price) VALUES (?, ?, ?, ?)");
            ps.setInt(1, importItem.getProduct().getProductId());
            ps.setInt(2, importItem.getQuantity());
            ps.setString(3, importItem.getImportDate());
            ps.setDouble(4, importItem.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getAllImports
    public List<Import> getAllImports() {
        List<Import> imports = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM importview");
            while (rs.next()) {
                Import importItem = new Import();
                importItem.setImportId(rs.getInt("importId"));
                importItem.getProduct().setProductId(rs.getInt("productId"));
                importItem.getProduct().setModel(rs.getString("model"));
                importItem.setQuantity(rs.getInt("quantity"));
                importItem.setPrice(rs.getDouble("price"));
                importItem.setTotalPrice(rs.getDouble("totalPrice"));
                importItem.setImportDate(rs.getString("importDate"));
                imports.add(importItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imports;
    }

    // updateImport
    public void updateImport(Import importItem) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE imports SET product_id=?, quantity=?, import_date=?, price=? WHERE import_id=?");
            ps.setInt(1, importItem.getProduct().getProductId());
            ps.setInt(2, importItem.getQuantity());
            ps.setString(3, importItem.getImportDate());
            ps.setDouble(4, importItem.getPrice());
            ps.setInt(5, importItem.getImportId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // deleteImport
    public void deleteImport(int importId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM imports WHERE import_id=?");
            ps.setInt(1, importId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getImportById
    public Import getImportById(int importId) {
        Import importItem = new Import();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM importview WHERE importId=?");
            ps.setInt(1, importId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                importItem.setImportId(rs.getInt("importId"));
                importItem.getProduct().setProductId(rs.getInt("productId"));
                importItem.getProduct().setModel(rs.getString("model"));
                importItem.setQuantity(rs.getInt("quantity"));
                importItem.setPrice(rs.getDouble("price"));
                importItem.setTotalPrice(rs.getDouble("totalPrice"));
                importItem.setImportDate(rs.getString("importDate"));            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return importItem;
    }
}