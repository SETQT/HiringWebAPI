package com.setqt.Hiring.Security.Model;

import java.io.Serializable;
import java.util.HashSet;

import org.hibernate.mapping.Set;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;
import jakarta.persistence.Table;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.setqt.Hiring.Security.Model.Role;

@Entity
@Table(name = "userInfo")
public class User implements Serializable {

	
	
	@Id
	@Column(name = "userId")
	@GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "sequence_User")
	@SequenceGenerator(name = "sequence_User", sequenceName = "sequence2", allocationSize = 1)
	private Long id;
	private String username;
	private String password;
	private Boolean isEnable;
	private static final long serialVersionUID = -297553281792804226L;
	
//	@JsonManagedReference
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "User_Role", joinColumns = { @JoinColumn(name = "user_Id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_Id") })
//	private Collection<Role> roles = new Collection ;
	private java.util.Set<Role> roles = new HashSet<>();
	

	public User(String username, String password, Boolean isEnable, Role roles) {
		super();
		this.username = username;
		this.password = password;
		this.isEnable = isEnable;
		this.roles.add( roles);
	}
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
		this.isEnable = true;

		this.roles=null;
//		RoleRepository roleRepository;
//		Role initRole = roleRepository.findRoleByName("Admin");
//		this.roles.add(new Role("ADMIN"));
	}

	public User() {
		super();

		this.username = null;
		this.password = null;
		this.isEnable = true;
		this.roles = null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

//	public void setRoles(java.util.Set<Role> roles) {
//		this.roles = roles;
//	}
	public void setRoles(Role roles) {
		this.roles.add(roles);
	}
//	public void setRoles(Collection<Role> roles) {
//		this.roles = roles;
//	}

}