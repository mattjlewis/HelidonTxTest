package org.test.tx.service;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.test.tx.model.Department;

@ApplicationScoped
public class DepartmentService {
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		// Make sure the many to one relationship is set
		department.getEmployees().forEach(emp -> emp.setDepartment(department));
		entityManager.persist(department);
		return department;
	}
	
	public Department get(final int id) {
		return entityManager.find(Department.class, Integer.valueOf(id));
	}
	
	public Department findByName(final String name) {
		return entityManager.createNamedQuery("Department.findByName", Department.class).setParameter("name", name).getSingleResult();
	}

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department update(final Department department) {
		return entityManager.merge(department);
	}
	
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		entityManager.remove(get(id));
	}
}
