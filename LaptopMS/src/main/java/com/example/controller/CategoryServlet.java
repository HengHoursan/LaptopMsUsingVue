package com.example.controller;

import com.example.dao.CategoryDAO;
import com.example.model.Category;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/category")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CategoryDAO categoryDAO;

    public void init() {
        categoryDAO = new CategoryDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        String jsonResponse;

        String categoryId = request.getParameter("category_id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");

        try {
            if (categoryId != null) {
                jsonResponse = gson.toJson(categoryDAO.getFilterCategory("category_id", categoryId));
            } else if (name != null) {
                jsonResponse = gson.toJson(categoryDAO.getFilterCategory("name", name));
            } else if (description != null) {
                jsonResponse = gson.toJson(categoryDAO.getFilterCategory("description", description));
            } else {
                if (categoryDAO.getAllCategories().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse = "{\"status\":\"error\", \"message\":\"No categories found\"}";
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonResponse = gson.toJson(categoryDAO.getAllCategories());
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to retrieve categories: " + e.getMessage() + "\"}";
        }

        PrintWriter out = response.getWriter();
        out.write(jsonResponse);
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

        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();

        Category category = new Category(name, description);

        response.setContentType("application/json");
        try {
            categoryDAO.addCategory(category);
            response.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = "{\"status\":\"success\", \"message\":\"Category created successfully\"}";
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to create category: " + e.getMessage() + "\"}";
            response.getWriter().write(jsonResponse);
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
        String jsonData = jsonBuffer.toString();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

        int categoryId = jsonObject.get("categoryId").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();

        Category category = new Category(name, description);
        category.setCategoryId(categoryId);

        response.setContentType("application/json");
        try {
            categoryDAO.updateCategory(category);
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = "{\"status\":\"success\", \"message\":\"Category updated successfully\"}";
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to update category: " + e.getMessage() + "\"}";
            response.getWriter().write(jsonResponse);
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
        String jsonData = jsonBuffer.toString();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();

        int categoryId = jsonObject.get("categoryId").getAsInt();
        response.setContentType("application/json");

        try {
            categoryDAO.deleteCategory(categoryId);
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = "{\"status\":\"success\", \"message\":\"Category deleted successfully\"}";
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String jsonResponse = "{\"status\":\"error\", \"message\":\"Failed to delete category: " + e.getMessage() + "\"}";
            response.getWriter().write(jsonResponse);
        }
    }
}