package org.test.tx.service;

import java.util.Optional;

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
		return "Department service - container managed & JTA";
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
	public Optional<Department> get(final int id) {
		return Optional.ofNullable(entityManager.find(Department.class, Integer.valueOf(id)));
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Optional<Department> findByName(final String name) {
		var results = entityManager.createNamedQuery("Department.findByName", Department.class)
				.setParameter("name", name).getResultList();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.get(0));
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department update(final Department department) {
		return entityManager.merge(department);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		entityManager.remove(entityManager.find(Department.class, Integer.valueOf(id)));
	}
}
