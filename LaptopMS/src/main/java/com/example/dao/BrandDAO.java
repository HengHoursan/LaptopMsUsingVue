package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Brand;
import com.example.util.DBUtil;

public class BrandDAO {
    private Connection connection;

    public BrandDAO() {
        connection = DBUtil.getConnection();
    }

    // addBrand
    public void addBrand(Brand brand) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO brand (name, description, image) VALUES (?, ?, ?)"
            );
            ps.setString(1, brand.getName());
            ps.setString(2, brand.getDescription());
            ps.setString(3, brand.getImage());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getAllBrands
    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM brand");
            while (rs.next()) {
                Brand brand = new Brand();
                brand.setBrandId(rs.getInt("brand_id"));
                brand.setName(rs.getString("name"));
                brand.setDescription(rs.getString("description"));
                brand.setImage(rs.getString("image"));
                brands.add(brand);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brands;
    }

    // updateBrand
    public void updateBrand(Brand brand) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE brand SET name=?, description=?, image=? WHERE brand_id=?"
            );
            ps.setString(1, brand.getName());
            ps.setString(2, brand.getDescription());
            ps.setString(3, brand.getImage());
            ps.setInt(4, brand.getBrandId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // deleteBrand
    public void deleteBrand(int brandId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM brand WHERE brand_id=?"
            );
            ps.setInt(1, brandId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getBrandById
    public Brand getBrandById(int brandId) {
        Brand brand = new Brand();
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM brand WHERE brand_id=?"
            );
            ps.setInt(1, brandId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                brand.setBrandId(rs.getInt("brand_id"));
                brand.setName(rs.getString("name"));
                brand.setDescription(rs.getString("description"));
                brand.setImage(rs.getString("image"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brand;
    }
}