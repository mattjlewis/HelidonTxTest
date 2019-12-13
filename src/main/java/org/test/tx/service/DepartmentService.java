package org.test.tx.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.test.tx.model.Department;

@ApplicationScoped
@Default
public class DepartmentService implements DepartmentServiceInterface {
	@PersistenceContext(unitName = "HelidonTxTestPuJta")
	private EntityManager entityManager;

	@Override
	public String getImplementation() {
		return "department service - container managed";
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		// Make sure the many to one relationship is set
		department.getEmployees().forEach(emp -> emp.setDepartment(department));
		entityManager.persist(department);
		return department;
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department get(final int id) {
		return entityManager.find(Department.class, Integer.valueOf(id));
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department findByName(final String name) {
		return entityManager.createNamedQuery("Department.findByName", Department.class).setParameter("name", name)
				.getSingleResult();
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department update(final Department department) {
		return entityManager.merge(department);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		Department dept = entityManager.merge(get(id));
		entityManager.remove(dept);
	}
}
