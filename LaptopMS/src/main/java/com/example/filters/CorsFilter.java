package com.example.filters;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/*")
public class CorsFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Optional initialization logic
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		LOGGER.info("CORSFilter HTTP Request: " + req.getMethod());

		HttpServletResponse resp = (HttpServletResponse) response;
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

		// Handle OPTIONS preflight request
		if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
			resp.setStatus(HttpServletResponse.SC_OK); // 200 OK for preflight requests
			return;
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Optional cleanup logic
	}
}