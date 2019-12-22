package org.test.tx.service;

import java.util.Date;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.test.tx.model.Department;
import org.test.tx.model.Employee;

@ApplicationScoped
@ResourceLocal
public class DepartmentServiceNonJta implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTxTestPuLocal")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "Department service - application managed EMF & resource local";
	}

	@Override
	public Department create(final Department department) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			// Make sure the many to one relationship is set
			if (department.getEmployees() != null) {
				department.getEmployees().forEach(emp -> emp.setDepartment(department));
			}
			Date now = new Date();
			department.setCreated(now);
			department.setLastUpdated(now);
			em.persist(department);
			tx.commit();
			return department;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public Department get(final int id) {
		EntityManager em = emf.createEntityManager();
		try {
			Department dept = em.find(Department.class, Integer.valueOf(id));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + id);
			}
			return dept;
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
			department.setLastUpdated(new Date());
			Department merged = em.merge(department);
			tx.commit();
			return merged;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
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
			Department dept = em.find(Department.class, Integer.valueOf(id));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + id);
			}
			em.remove(dept);
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void addEmploye(int departmentId, Employee employee) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			Department dept = em.find(Department.class, Integer.valueOf(departmentId));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + departmentId);
			}
			employee.setDepartment(dept);
			em.persist(employee);
			dept.getEmployees().add(employee);
			dept.setLastUpdated(new Date());
			// FIXME Do I need to do this?
			//em.merge(dept);
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void removeEmployee(int departmentId, int employeeId) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			Department dept = em.find(Department.class, Integer.valueOf(departmentId));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + departmentId);
			}
			Optional<Employee> opt_emp = dept.getEmployees().stream()
					.filter(emp -> emp.getId().intValue() == employeeId).findFirst();
			Employee emp = opt_emp.orElseThrow(() -> new EntityNotFoundException(
					"No such Employee with id " + employeeId + " in department " + departmentId));
			emp.setDepartment(null);
			dept.getEmployees().remove(emp);
			dept.setLastUpdated(new Date());
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}
}
