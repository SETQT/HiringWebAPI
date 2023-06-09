package com.setqt.Hiring.DTO;

public class EmployeeAuthedDTO {
    private String email;
    private String password;
    private String name;
    private String address;
    private String companySize;
    private String phone;
    private String domain;
    private String workTime;


    public EmployeeAuthedDTO(String email, String password, String name, String address, String companySize, String phone, String domain, String workTime) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.companySize = companySize;
        this.phone = phone;
        this.domain = domain;
        this.workTime = workTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }
}
