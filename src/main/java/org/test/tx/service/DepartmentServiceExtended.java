package org.test.tx.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

import org.test.tx.model.Department;

@ApplicationScoped
@ExtendedPu
public class DepartmentServiceExtended implements DepartmentServiceInterface {
	@PersistenceContext(unitName = "HelidonTxTestPuJta", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	@Override
	public String getImplementation() {
		return "department service - extended";
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		try {
			// Make sure the many to one relationship is set
			department.getEmployees().forEach(emp -> emp.setDepartment(department));
			entityManager.persist(department);
			return department;
		} finally {
			entityManager.clear();
		}
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
		try {
			return entityManager.merge(department);
		} finally {
			entityManager.clear();
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		try {
			entityManager.remove(entityManager.find(Department.class, Integer.valueOf(id)));
		} finally {
			entityManager.clear();
		}
	}
}
