package com.example.controller;

import com.example.dao.EmployeeDAO;
import com.example.model.Employee;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/employee")
public class EmployeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EmployeeDAO employeeDAO;

	public void init() {
		employeeDAO = new EmployeeDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		String jsonResponse;

		String employeeId = request.getParameter("employeeId");
		String name = request.getParameter("name");
		String gender = request.getParameter("gender");
		String position = request.getParameter("position");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");

		try {
			if (employeeId != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("employeeId", employeeId));
			} else if (name != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("name", name));
			}else if(gender != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("gender", gender));
			}else if (position != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("position", position));
			} else if (email != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("email", email));
			} else if (phone != null) {
				jsonResponse = gson.toJson(employeeDAO.getFilteredEmployees("phone", phone));
			} else {
				jsonResponse = gson.toJson(employeeDAO.getAllEmployees());
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to retrieve employees: " + e.getMessage()
					+ "\"}";
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
		String gender = jsonObject.get("gender").getAsString();
		String position = jsonObject.get("position").getAsString();
		String email = jsonObject.get("email").getAsString();
		String phone = jsonObject.get("phone").getAsString();

		Employee employee = new Employee(name, gender, position, email, phone);
		response.setContentType("application/json");

		try {
			employeeDAO.addEmployee(employee);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.getWriter().write("{\"status\":\"success\", \"message\":\"Employee created successfully\"}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter()
					.write("{\"status\":\"error\", \"message\":\"Failed to create employee: " + e.getMessage() + "\"}");
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

		int employeeId = jsonObject.get("employeeId").getAsInt();
		String name = jsonObject.get("name").getAsString();
		String gender = jsonObject.get("gender").getAsString();
		String position = jsonObject.get("position").getAsString();
		String email = jsonObject.get("email").getAsString();
		String phone = jsonObject.get("phone").getAsString();
		Employee employee = new Employee(name, gender, position, email, phone);
	
		employee.setEmployeeId(employeeId);
		response.setContentType("application/json");

		try {
			employeeDAO.updateEmployee(employee);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write("{\"status\":\"success\", \"message\":\"Employee updated successfully\"}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter()
					.write("{\"status\":\"error\", \"message\":\"Failed to update employee: " + e.getMessage() + "\"}");
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

		int employeeId = jsonObject.get("employeeId").getAsInt();
		response.setContentType("application/json");

		try {
			employeeDAO.deleteEmployee(employeeId);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write("{\"status\":\"success\", \"message\":\"Employee deleted successfully\"}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter()
					.write("{\"status\":\"error\", \"message\":\"Failed to delete employee: " + e.getMessage() + "\"}");
		}
	}

}
