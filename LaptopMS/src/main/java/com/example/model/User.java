package com.example.model;

public class User {
	private int userid;
	private String username;
	private String email;
	private String password;
	private String address;

	public User() {
	}

	public User(String userName, String email, String password,String address) {
		this.username = userName;
		this.email = email;
		this.password = password;
		this.address = address;
	}
	public int getId () {
		return userid;
	}
	public void setId (int userid) 
	{
		this.userid = userid;
	}
	public String getUserName () {
		return username;
	}
	public void setUserName (String username) {
		this.username = username;
	}
	public String getEmail () {
		return email;
	}
	public void setEmail (String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAddress () {
		return address;
	}
	public void setAddress (String address) {
		this.address = address;
	}
}
