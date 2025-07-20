package com.example.controller;

import com.example.dao.CustomerDAO;
import com.example.model.Customer;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/customer")
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CustomerDAO customerDAO;

    public void init() {
        customerDAO = new CustomerDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        String jsonResponse;

        String customerId = request.getParameter("customerId");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        try {
            if (customerId != null) {
                jsonResponse = gson.toJson(customerDAO.getFilterCustomer("customerId", customerId));
            } else if (name != null) {
                jsonResponse = gson.toJson(customerDAO.getFilterCustomer("name", name));
            } else if (email != null) {
                jsonResponse = gson.toJson(customerDAO.getFilterCustomer("email", email));
            } else if (phone != null) {
                jsonResponse = gson.toJson(customerDAO.getFilterCustomer("phone", phone));
            } else if (address != null) {
                jsonResponse = gson.toJson(customerDAO.getFilterCustomer("address", address));
            } else {
                jsonResponse = gson.toJson(customerDAO.getAllCustomers());
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to retrieve customers: " + e.getMessage() + "\"}";
        }

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

        String name = jsonObject.get("name").getAsString();
        String email = jsonObject.get("email").getAsString();
        String phone = jsonObject.get("phone").getAsString();
        String address = jsonObject.get("address").getAsString();

        Customer customer = new Customer(name, email, phone, address);
        response.setContentType("application/json");
        try {
            customerDAO.addCustomer(customer);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"status\":\"success\", \"message\":\"Customer created successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to create customer: " + e.getMessage() + "\"}");
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
        
        int customerId = jsonObject.get("customerId").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String email = jsonObject.get("email").getAsString();
        String phone = jsonObject.get("phone").getAsString();
        String address = jsonObject.get("address").getAsString();

        Customer customer = new Customer(name, email, phone, address);
        customer.setCustomerId(customerId);

        response.setContentType("application/json");
        try {
            customerDAO.updateCustomer(customer);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"success\", \"message\":\"Customer updated successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to update customer: " + e.getMessage() + "\"}");
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

        int customerId = jsonObject.get("customerId").getAsInt();
        response.setContentType("application/json");

        try {
            customerDAO.deleteCustomer(customerId);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"success\", \"message\":\"Customer deleted successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to delete customer: " + e.getMessage() + "\"}");
        }
    }
}
