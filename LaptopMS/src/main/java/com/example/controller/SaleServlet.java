package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.dao.SaleDAO;
import com.example.model.Sale;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/sale")
public class SaleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SaleDAO saleDAO;

	public void init() {
		saleDAO = new SaleDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		String json;
		String saleId = request.getParameter("saleId");
		if (saleId != null) {
			json = gson.toJson(saleDAO.getSaleById(Integer.parseInt(saleId)));
		} else {
			json = gson.toJson(saleDAO.getAllSales());
		}
		PrintWriter out = response.getWriter();
		out.write(json);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}

		try {
			String jsonData = jsonBuffer.toString();
			JsonParser parser = new JsonParser();
			JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

			int productId = jsonObject.get("productId").getAsInt();
			int customerId = jsonObject.get("customerId").getAsInt();
			int employeeId = jsonObject.get("employeeId").getAsInt();
			int quantity = jsonObject.get("quantity").getAsInt();
			double price = jsonObject.get("price").getAsDouble();
			String saleDate = jsonObject.get("saleDate").getAsString();

			Sale sale = new Sale();
			sale.getProduct().setProductId(productId);
			sale.getCustomer().setCustomerId(customerId);
			sale.getEmployee().setEmployeeId(employeeId);
			sale.setQuantity(quantity);
			sale.setPrice(price);
			sale.setSaleDate(saleDate);

			boolean success = saleDAO.addSale(sale);

			response.setContentType("application/json");
			PrintWriter out = response.getWriter();

			if (success) {
				out.write("{\"message\": \"Insert success\"}");
			} else {
				out.write("{\"error\": \"Not enough stock available or product not found\"}");
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"error\": \"Internal server error\"}");
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}

		try {
			String jsonData = jsonBuffer.toString();
			JsonParser parser = new JsonParser();
			JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

			int saleId = jsonObject.get("saleId").getAsInt();
			int productId = jsonObject.get("productId").getAsInt();
			int customerId = jsonObject.get("customerId").getAsInt();
			int employeeId = jsonObject.get("employeeId").getAsInt();
			int quantity = jsonObject.get("quantity").getAsInt();
			double price = jsonObject.get("price").getAsDouble();
			String saleDate = jsonObject.get("saleDate").getAsString();

			Sale sale = new Sale();
			sale.setSaleId(saleId);
			sale.getProduct().setProductId(productId);
			sale.getCustomer().setCustomerId(customerId);
			sale.getEmployee().setEmployeeId(employeeId);
			sale.setQuantity(quantity);
			sale.setPrice(price);
			sale.setSaleDate(saleDate);

			boolean success = saleDAO.updateSale(sale);

			response.setContentType("application/json");
			PrintWriter out = response.getWriter();

			if (success) {
				out.write("{\"message\": \"Update success\"}");
			} else {
				out.write("{\"error\": \"Not enough stock available or sale not found\"}");
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"error\": \"Internal server error\"}");
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuilder jsonBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = request.getReader()) {
			while ((line = reader.readLine()) != null) {
				jsonBuffer.append(line);
			}
		}

		try {
			String jsonData = jsonBuffer.toString();
			JsonParser parser = new JsonParser();
			JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

			int saleId = jsonObject.get("saleId").getAsInt();
			saleDAO.deleteSale(saleId);

			response.setContentType("application/json");
			response.getWriter().write("{\"message\": \"Delete success\"}");
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"error\": \"Internal server error\"}");
		}
	}

}