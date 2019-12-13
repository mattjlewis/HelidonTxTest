package org.test.tx.service;

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
			Department dept = entityManager.merge(entityManager.find(Department.class, Integer.valueOf(id)));
			entityManager.remove(dept);
		} finally {
			entityManager.clear();
		}
	}
}
