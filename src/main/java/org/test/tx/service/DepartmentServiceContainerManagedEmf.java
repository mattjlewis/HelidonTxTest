package org.test.tx.service;

import java.util.Date;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import org.test.tx.model.Department;
import org.test.tx.model.Employee;

@ApplicationScoped
@ContainerManagedEmf
public class DepartmentServiceContainerManagedEmf implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTxTestPuJta")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "Department service - container managed EMF & JTA";
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			// Make sure the many to one relationship is set
			if (department.getEmployees() != null) {
				department.getEmployees().forEach(emp -> emp.setDepartment(department));
			}
			Date now = new Date();
			department.setCreated(now);
			department.setLastUpdated(now);
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
			Department dept = em.find(Department.class, Integer.valueOf(id));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + id);
			}
			return dept;
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
			department.setLastUpdated(new Date());
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
			Department dept = em.find(Department.class, Integer.valueOf(id));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + id);
			}
			em.remove(dept);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void addEmploye(int departmentId, Employee employee) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
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
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void removeEmployee(int departmentId, int employeeId) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
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
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}
}
