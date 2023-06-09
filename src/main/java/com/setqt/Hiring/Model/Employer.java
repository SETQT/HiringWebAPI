package com.setqt.Hiring.Model;

import java.io.Serializable;

import com.setqt.Hiring.Security.Model.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.ToString;

@Entity
@Table(name="employer")
@ToString
public class Employer implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private static final long serialVersionUID = -297553781792804396L;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",referencedColumnName = "userId")
	@JsonBackReference(value="user_employer")
	private User user;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name="company_id", referencedColumnName = "id")
	@JsonManagedReference(value="company_employer")
	private Company company;


	private String phone;
	private String email;
	@Column(columnDefinition="TEXT")
	private String logo;

	public Employer(User user, Company company, String phone, String email, String logo) {
		this.user = user;
		this.company = company;
		this.phone = phone;
		this.email = email;
		this.logo = logo;
	}

	public Employer(Long id,User u, String phone, String email, String logo) {
//		this.company=com;
		this.id=id;
		this.user=u;
		this.phone = phone;
		this.email = email;
		this.logo = logo;
	}

	public Employer() {

	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getCompanyId() {
//		return company;
		if (company!= null)
		return company.getId();
		else return null;
	}

	public Long getUserId(){
		return user.getId();
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
