package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import com.example.dao.ProductDAO;
import com.example.model.Product;
import com.example.model.Brand;
import com.example.model.Category;


import com.google.gson.Gson;

@WebServlet("/product")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 50) // 50MB
public class ProductServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String UPLOAD_DIR = "uploads";

	private ProductDAO productDAO;

	public void init() {
		productDAO = new ProductDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		String json;
		String productId = request.getParameter("productId");
		if (productId != null) {
			json = gson.toJson(productDAO.getProductById(Integer.parseInt(productId)));
		} else {
			json = gson.toJson(productDAO.getAllProducts());
		}
		PrintWriter out = response.getWriter();
		out.write(json);
		out.flush();
	}

	private String uploadPhoto(HttpServletRequest request) throws IOException, ServletException {
		// 1. Define the upload directory path
		String uploadPath = Paths.get(getServletContext().getRealPath(""), UPLOAD_DIR).toString();
		System.out.println("Upload Path: " + uploadPath); // Debug log

		// 2. Create the upload directory if it doesn't exist
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			boolean created = uploadDir.mkdir();
			System.out.println("Upload directory created: " + created); // Debug log
		}

		// 3. Retrieve the uploaded file part
		Part filePart = request.getPart("image");
		if (filePart == null) {
			System.out.println("No file part found in the request."); // Debug log
			return null;
		}

		// 4. Get the submitted file name
		String fileName = filePart.getSubmittedFileName();
		System.out.println("File Name: " + fileName); // Debug log

		// 5. Check if a file was uploaded
		if (fileName == null || fileName.isEmpty()) {
			System.out.println("No file uploaded."); // Debug log
			return null;
		}

		// 6. Save the file to the upload directory
		String filePath = uploadPath + File.separator + fileName;
		filePart.write(filePath);
		System.out.println("File saved to: " + filePath); // Debug log

		// 7. Return the relative path of the saved file
		return UPLOAD_DIR + File.separator + fileName;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    try {
	        int brandId = Integer.parseInt(request.getParameter("brandId"));
	        int categoryId = Integer.parseInt(request.getParameter("categoryId"));
	        double price = Double.parseDouble(request.getParameter("price"));
	        String model = request.getParameter("model");
	        String description = request.getParameter("description");
	        String details = request.getParameter("details");

	        String image = uploadPhoto(request);

	        Product product = new Product();

	        Brand brand = new Brand();
	        brand.setBrandId(brandId);
	        product.setBrand(brand);

	        Category category = new Category();
	        category.setCategoryId(categoryId);
	        product.setCategory(category);

	        product.setPrice(price);
	        product.setModel(model);
	        product.setDescription(description);
	        product.setDetails(details);
	        product.setImage(image);

	        productDAO.addProduct(product);

	        response.setContentType("application/json");
	        response.getWriter().write("{\"message\": \"insert success\"}");
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().write("{\"message\": \"Server error occurred. Please try again.\"}");
	    }
	}


	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int productId = Integer.parseInt(request.getParameter("productId"));
		int brandId = Integer.parseInt(request.getParameter("brandId"));
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		double price = Double.parseDouble(request.getParameter("price"));
		String model = request.getParameter("model");
		String description = request.getParameter("description");
		String details = request.getParameter("details");
		String image = uploadPhoto(request);

		Product existingProduct = productDAO.getProductById(productId);
		if (image == null) {
			image = existingProduct.getImage();
		}

		Product product = new Product();
		product.setProductId(productId);
		
		Brand brand = new Brand();
        brand.setBrandId(brandId);
        product.setBrand(brand);

        Category category = new Category();
        category.setCategoryId(categoryId);
        product.setCategory(category);
        
		product.setPrice(price);
		product.setModel(model);
		product.setDescription(description);
		product.setDetails(details);
		product.setImage(image);
		productDAO.updateProduct(product);

		response.setContentType("application/json");
		response.getWriter().write("{\"message\": \"update success\"}");
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int productId = Integer.parseInt(request.getParameter("productId"));
		productDAO.deleteProduct(productId);

		response.setContentType("application/json");
		response.getWriter().write("{\"message\": \"delete success\"}");
	}
}
