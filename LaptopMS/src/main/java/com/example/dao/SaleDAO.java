package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Sale;
import com.example.util.DBUtil;

public class SaleDAO {
    private Connection connection;

    public SaleDAO() {
        connection = DBUtil.getConnection();
    }

    // addSale
    // public void addSale(Sale sale) {
    //    try {
    //        PreparedStatement ps = connection.prepareStatement(
    //                "INSERT INTO sales (product_id, customer_id, employee_id, quantity, price, sale_date) VALUES (?, ?, ?, ?, ?, ?)");
    //        ps.setInt(1, sale.getProduct().getProductId());
    //        ps.setInt(2, sale.getCustomer().getCustomerId());
    //        ps.setInt(3, sale.getEmployee().getEmployeeId());
    //        ps.setInt(4, sale.getQuantity());
    //        ps.setDouble(5, sale.getPrice());
    //        ps.setDate(6, new java.sql.Date(sale.getSaleDate().getTime()));
    //        ps.executeUpdate();
    //    } catch (SQLException e) {
    //        e.printStackTrace();
    //    }
    // }

    // Add Sale with Stock Check
    public boolean addSale(Sale sale) {
        try {
            int productId = sale.getProduct().getProductId();
            int quantity = sale.getQuantity();

            // Check if product stock is available
            PreparedStatement stockStmt = connection.prepareStatement("SELECT stock FROM productview WHERE productId = ?");
            stockStmt.setInt(1, productId);
            ResultSet stockResult = stockStmt.executeQuery();

            if (stockResult.next()) {
                int availableStock = stockResult.getInt("stock");
                if (availableStock < quantity) {
                    System.out.println("Sale cannot be completed. Not enough stock available.");
                    return false;
                }
            } else {
                System.out.println("Product not found.");
                return false;
            }

            // Insert sale
            PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO sales (product_id, customer_id, employee_id, quantity, price, sale_date) VALUES (?, ?, ?, ?, ?, ?)"
            );
            insertStmt.setInt(1, productId);
            insertStmt.setInt(2, sale.getCustomer().getCustomerId());
            insertStmt.setInt(3, sale.getEmployee().getEmployeeId());
            insertStmt.setInt(4, quantity);
            insertStmt.setDouble(5, sale.getPrice());
            insertStmt.setString(6, sale.getSaleDate());  // Changed to set String saleDate
            insertStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update Sale with Stock Validation
    public boolean updateSale(Sale sale) {
        try {
            int saleId = sale.getSaleId();
            int productId = sale.getProduct().getProductId();
            int newQuantity = sale.getQuantity();

            // Get the existing sale quantity
            PreparedStatement currentSaleStmt = connection.prepareStatement("SELECT quantity FROM sales WHERE sale_id = ?");
            currentSaleStmt.setInt(1, saleId);
            ResultSet saleResult = currentSaleStmt.executeQuery();

            if (!saleResult.next()) {
                System.out.println("Sale not found.");
                return false;
            }
            int oldQuantity = saleResult.getInt("quantity");

            // Check available stock
            PreparedStatement stockStmt = connection.prepareStatement("SELECT stock FROM productview WHERE productId = ?");
            stockStmt.setInt(1, productId);
            ResultSet stockResult = stockStmt.executeQuery();

            if (stockResult.next()) {
                int availableStock = stockResult.getInt("stock");
                int adjustedStock = availableStock + oldQuantity - newQuantity;

                if (adjustedStock < 0) {
                    System.out.println("Sale update failed. Not enough stock available.");
                    return false;
                }
            } else {
                System.out.println("Product not found.");
                return false;
            }

            // Update sale
            PreparedStatement updateStmt = connection.prepareStatement(
                "UPDATE sales SET product_id=?, customer_id=?, employee_id=?, quantity=?, price=?, sale_date=? WHERE sale_id=?"
            );
            updateStmt.setInt(1, productId);
            updateStmt.setInt(2, sale.getCustomer().getCustomerId());
            updateStmt.setInt(3, sale.getEmployee().getEmployeeId());
            updateStmt.setInt(4, newQuantity);
            updateStmt.setDouble(5, sale.getPrice());
            updateStmt.setString(6, sale.getSaleDate());
            updateStmt.setInt(7, saleId);
            updateStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // getAllSales
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM saleview");
            while (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(rs.getInt("saleId"));
                sale.getProduct().setModel(rs.getString("model"));
                sale.setQuantity(rs.getInt("quantity"));                
                sale.setPrice(rs.getDouble("price"));
                sale.setTotalPrice(rs.getDouble("totalPrice"));
                sale.setSaleDate(rs.getString("saleDate"));
                sale.getCustomer().setName(rs.getString("customer"));
                sale.getEmployee().setName(rs.getString("soldBy"));
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    // Delete sale
    public void deleteSale(int saleId) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM sales WHERE sale_id=?");
            ps.setInt(1, saleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get sale by ID
    public Sale getSaleById(int saleId) {
        Sale sale = new Sale();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM sales WHERE sale_id=?");
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sale.setSaleId(rs.getInt("sale_id"));
                sale.getProduct().setProductId(rs.getInt("product_id"));
                sale.getCustomer().setCustomerId(rs.getInt("customer_id"));
                sale.getEmployee().setEmployeeId(rs.getInt("employee_id"));
                sale.setQuantity(rs.getInt("quantity"));
                sale.setTotalPrice(rs.getDouble("price"));
                sale.setSaleDate(rs.getString("sale_date"));  // Changed to set String saleDate
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sale;
    }
}
