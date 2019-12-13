package org.test.tx.service;

import org.test.tx.model.Department;

public interface DepartmentServiceInterface {
	String getImplementation();
	
	Department create(final Department department);

	Department get(final int id);

	Department findByName(final String name);

	Department update(final Department department);

	void remove(final int id);
}
