package org.test.tx.service;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import org.test.tx.model.Department;

@ApplicationScoped
@AppManaged
public class DepartmentServiceAppManaged implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTxTestPuJta")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "department service - application managed";
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			// Make sure the many to one relationship is set
			department.getEmployees().forEach(emp -> emp.setDepartment(department));
			em.persist(department);
			return department;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Optional<Department> get(final int id) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return Optional.ofNullable(em.find(Department.class, Integer.valueOf(id)));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Optional<Department> findByName(final String name) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			var results = em.createNamedQuery("Department.findByName", Department.class).setParameter("name", name).getResultList();
			if (results.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(results.get(0));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department update(final Department department) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			Department merged = em.merge(department);
			return merged;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			em.remove(em.find(Department.class, Integer.valueOf(id)));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}
}
