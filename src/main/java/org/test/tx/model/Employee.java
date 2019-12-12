package org.test.tx.model;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Employee {
	@Id
	@GeneratedValue(generator = "EmployeeSeq", strategy = GenerationType.SEQUENCE)
	private Integer id;
	@Column(length = 20, nullable = false)
	@Basic(optional = false)
	private String name;
	@Column(length = 20, nullable = false)
	@Basic(optional = false)
	private String emailAddress;
	@Column(length = 20, nullable = true)
	@Basic(optional = true)
	private String favouriteDrink;
	@ManyToOne
	@JoinColumn(name = "DEPARTMENT_ID", nullable = false)
	@JsonbTransient
	private Department department;
	
	public Employee() {
	}

	public Employee(String name, String emailAddress, String favouriteDrink) {
		this.name = name;
		this.emailAddress = emailAddress;
		this.favouriteDrink = favouriteDrink;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}



	public String getFavouriteDrink() {
		return favouriteDrink;
	}



	public void setFavouriteDrink(String favouriteDrink) {
		this.favouriteDrink = favouriteDrink;
	}



	public Department getDepartment() {
		return department;
	}



	public void setDepartment(Department department) {
		this.department = department;
	}
}
