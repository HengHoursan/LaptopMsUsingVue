package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.example.dao.ImportDAO;
import com.example.model.Import;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/import")
public class ImportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ImportDAO importDAO;

    public void init() {
        importDAO = new ImportDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        String json;
        String importId = request.getParameter("importId");
        if (importId != null) {
            json = gson.toJson(importDAO.getImportById(Integer.parseInt(importId)));
        } else {
            json = gson.toJson(importDAO.getAllImports());
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

        String jsonData = jsonBuffer.toString();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

        int productId = jsonObject.get("productId").getAsInt();
        int quantity = jsonObject.get("quantity").getAsInt();
        double price = jsonObject.get("price").getAsDouble();
        String importDate = jsonObject.get("importDate").getAsString();

        Import importItem = new Import();
        importItem.getProduct().setProductId(productId);
        importItem.setQuantity(quantity);
        importItem.setPrice(price);
        importItem.setImportDate(importDate);
        importDAO.addImport(importItem);

        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"insert success\"}");
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

        String jsonData = jsonBuffer.toString();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

        int importId = jsonObject.get("importId").getAsInt();
        int productId = jsonObject.get("productId").getAsInt();
        int quantity = jsonObject.get("quantity").getAsInt();
        double price = jsonObject.get("price").getAsDouble();
        String importDate = jsonObject.get("importDate").getAsString();

        Import importItem = new Import();
        importItem.setImportId(importId);
        importItem.getProduct().setProductId(productId);
        importItem.setQuantity(quantity);
        importItem.setPrice(price);
        importItem.setImportDate(importDate);
        importDAO.updateImport(importItem);

        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"update success\"}");
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

        String jsonData = jsonBuffer.toString();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

        int importId = jsonObject.get("importId").getAsInt();
        importDAO.deleteImport(importId);

        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"delete success\"}");
    }
}