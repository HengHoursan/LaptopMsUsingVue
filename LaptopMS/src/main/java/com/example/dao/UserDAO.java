package com.example.dao;

import com.example.model.User;
import com.example.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
	private Connection connection;

	public UserDAO() {
		connection = DBUtil.getConnection();
	}

	// get all users
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM user");
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("userid"));
				user.setUserName(rs.getString("username"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setAddress(rs.getString("address"));
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}
	// Get User by field 
	public List<User> getFilterUser(String field, String searchText) {
	    List<User> users = new ArrayList<>();
	    try {
	        // Determine the SQL query based on the field
	        String sql = "";
	        switch (field) {
	            case "userid":
	                sql = "SELECT * FROM users WHERE userid=?";
	                break;
	            case "username":
	                sql = "SELECT * FROM users WHERE username LIKE CONCAT('%', ?, '%')";
	                break;
	            case "email":
	                sql = "SELECT * FROM users WHERE email LIKE CONCAT('%', ?, '%')";
	                break;
	            case "address":
	                sql = "SELECT * FROM users WHERE address LIKE CONCAT('%', ?, '%')";
	                break;
	            default:
	                throw new IllegalArgumentException("Invalid field: " + field);
	        }

	        // Prepare and execute the query
	        PreparedStatement ps = connection.prepareStatement(sql);
	        if (field.equals("userid")) {
	            ps.setInt(1, Integer.parseInt(searchText));  // Set id as an integer
	        } else {
	            ps.setString(1, searchText);  // Set other fields as a string
	        }

	        // Execute the query and process the result set
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            User user = new User();
	            user.setId(rs.getInt("userid"));
	            user.setUserName(rs.getString("username"));
	            user.setEmail(rs.getString("email"));
	            user.setPassword(rs.getString("password"));
	            user.setAddress(rs.getString("address"));
	            users.add(user);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return users;
	}

	// insert new user
	public void addUser(User user) {
		try {
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO user (username,email,password,address) VALUES (?,?,?,?)");
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPassword());
			ps.setString(4, user.getAddress());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// update user
	public void updateUser(User user) {
		try {
			PreparedStatement ps = connection
					.prepareStatement("UPDATE user SET username=?, email=?, password=?, address=? WHERE userid=?");
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPassword());
			ps.setString(4, user.getAddress());
			ps.setInt(5, user.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// delete user
	public void deleteUser(int userid) {
		try {
			PreparedStatement ps = connection.prepareStatement("DELETE FROM user WHERE userid=?");
			ps.setInt(1, userid);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// get user by id
	public User getUserById (int userid) {
		User user = new User();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM user WHERE userid=?");
			ps.setInt(1, userid);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				user.setId(rs.getInt("userid"));
				user.setUserName(rs.getString("username"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setAddress(rs.getString("address"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}
}
