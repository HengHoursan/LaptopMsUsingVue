package com.example.controller;

import com.example.dao.UserDAO;
import com.example.model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/user")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private UserDAO userDAO;

	public void init() {
		userDAO = new UserDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// Initialize Gson for converting objects to JSON
		Gson gson = new Gson();
		String jsonResponse;

		// Get parameters from the request (for filtering)
		String userid = request.getParameter("userid");
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String address = request.getParameter("address");

		try {
			// Determine the filter criteria and fetch users accordingly
			if (userid != null) {
				// Filter users by ID
				jsonResponse = gson.toJson(userDAO.getFilterUser("userid", userid));
			} else if (username != null) {
				// Filter users by username
				jsonResponse = gson.toJson(userDAO.getFilterUser("username", username));
			} else if (email != null) {
				// Filter users by email
				jsonResponse = gson.toJson(userDAO.getFilterUser("email", email));
			} else if (address != null) {
				// Filter users by address
				jsonResponse = gson.toJson(userDAO.getFilterUser("address", address));
			} else {
				// If no filter is applied, fetch all users
				if (userDAO.getAllUsers().isEmpty()) {
					// Set status code to 404 (Not Found) if no users are found
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					jsonResponse = "{\"status\":\"error\", \"message\":\"No users found\"}";
				} else {
					// Set status code to 200 (OK) and return the list of users
					response.setStatus(HttpServletResponse.SC_OK);
					jsonResponse = gson.toJson(userDAO.getAllUsers());
				}
			}
		} catch (Exception e) {
			// Set status code to 500 (Internal Server Error) in case of an exception
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to retrieve users: " + e.getMessage() + "\"}";
		}

		// Write the JSON response
		PrintWriter out = response.getWriter();
		out.write(jsonResponse);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Read JSON data from the request
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}
		String jsonData = jsonBuffer.toString();
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

		// Extract data from the JSON object
		String username = jsonObject.get("username").getAsString();
		String email = jsonObject.get("email").getAsString();
		String password = jsonObject.get("password").getAsString();
		String address = jsonObject.get("address").getAsString();

		// Create a User object
		User user = new User(username, email, password, address);

		// Initialize the response
		response.setContentType("application/json");
		try {
			// Add the user to the database
			userDAO.addUser(user);

			// Set response status code for success (201 Created)
			response.setStatus(HttpServletResponse.SC_CREATED);

			// Send a success response with a JSON message
			String jsonResponse = "{\"status\":\"success\", \"message\":\"User created successfully\"}";
			response.getWriter().write(jsonResponse);

		} catch (Exception e) {
			// Set response status code for failure (500 Internal Server Error)
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			// Send an error response with a JSON message
			String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to create user: " + e.getMessage()
					+ "\"}";
			response.getWriter().write(jsonResponse);
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Read JSON data from the request
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}
		String jsonData = jsonBuffer.toString();
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();
		int userid = jsonObject.get("userid").getAsInt();
		String username = jsonObject.get("username").getAsString();
		String email = jsonObject.get("email").getAsString();
		String password = jsonObject.get("password").getAsString();
		String address = jsonObject.get("address").getAsString();

		// Create a User object
		User user = new User(username, email, password, address);
		user.setId(userid);

		// Initialize the response
		response.setContentType("application/json");
		try {
			// Attempt to update the user in the database
			userDAO.updateUser(user);

			// Set response status code for success (200 OK)
			response.setStatus(HttpServletResponse.SC_OK);

			// Send a success response with a JSON message
			String jsonResponse = "{\"status\":\"success\", \"message\":\"User updated successfully\"}";
			response.getWriter().write(jsonResponse);
		} catch (Exception e) {
			// Set response status code for failure (404 Not Found or other errors)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			// Send an error response with a JSON message
			String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to update user: " + e.getMessage()
					+ "\"}";
			response.getWriter().write(jsonResponse);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Read JSON data from the request
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}
		String jsonData = jsonBuffer.toString();
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

		int userid = jsonObject.get("userid").getAsInt();
		response.setContentType("application/json");

		try {
			// Attempt to delete the user from the database
			userDAO.deleteUser(userid);

			// Set response status code for success (200 No Content)
			response.setStatus(HttpServletResponse.SC_OK);

			// Send a success response with a JSON message
			String jsonResponse = "{\"status\":\"success\", \"message\":\"User deleted successfully\"}";
			response.getWriter().write(jsonResponse);
		} catch (Exception e) {
			// Set response status code for failure (404 Not Found or other errors)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			// Send an error response with a JSON message
			String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to delete user: " + e.getMessage()
					+ "\"}";
			response.getWriter().write(jsonResponse);
		}
	}
}
