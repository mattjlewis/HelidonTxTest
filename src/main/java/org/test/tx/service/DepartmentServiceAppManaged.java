package org.test.tx.service;

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
	public Department get(final int id) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return em.find(Department.class, Integer.valueOf(id));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department findByName(final String name) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return em.createNamedQuery("Department.findByName", Department.class).setParameter("name", name)
					.getSingleResult();
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
			Department dept = em.merge(em.find(Department.class, Integer.valueOf(id)));
			em.remove(dept);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}
}
