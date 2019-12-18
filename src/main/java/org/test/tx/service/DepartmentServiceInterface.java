package org.test.tx.service;

import java.util.Optional;

import org.test.tx.model.Department;

public interface DepartmentServiceInterface {
	String getImplementation();
	
	Department create(final Department department);

	Optional<Department> get(final int id);

	Optional<Department> findByName(final String name);

	Department update(final Department department);

	void remove(final int id);
}
