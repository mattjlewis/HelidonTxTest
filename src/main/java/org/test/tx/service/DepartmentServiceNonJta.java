package org.test.tx.service;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.test.tx.model.Department;

@ApplicationScoped
@ResourceLocal
public class DepartmentServiceNonJta implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTxTestPuLocal")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "department service - resource local";
	}

	@Override
	public Department create(final Department department) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			// Make sure the many to one relationship is set
			department.getEmployees().forEach(emp -> emp.setDepartment(department));
			em.persist(department);
			tx.commit();
			return department;
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public Department get(final int id) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.find(Department.class, Integer.valueOf(id));
		} finally {
			em.close();
		}
	}

	@Override
	public Department findByName(final String name) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createNamedQuery("Department.findByName", Department.class).setParameter("name", name)
					.getSingleResult();
		} finally {
			em.close();
		}
	}

	@Override
	public Department update(final Department department) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			Department dept = em.merge(department);
			tx.commit();
			return dept;
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void remove(final int id) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			em.remove(em.find(Department.class, Integer.valueOf(id)));
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}
}
