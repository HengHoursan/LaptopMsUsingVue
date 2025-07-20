package com.example.model;

public class Employee {
    private int employeeId;
    private String name;
    private String position;
    private String email;
    private String phone;
    private String gender;

    public Employee() {
    }

    public Employee(String name,String gender, String position, String email, String phone) {
        this.name = name;
        this.gender = gender;
        this.position = position;
        this.email = email;
        this.phone = phone;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getGender() {
    	return gender;
    }
    public void setGender(String gender) {
    	this.gender = gender;
    }
}
