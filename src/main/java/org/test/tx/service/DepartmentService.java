package org.test.tx.service;

import java.util.Date;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.test.tx.model.Department;
import org.test.tx.model.Employee;

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
		if (department.getEmployees() != null) {
			department.getEmployees().forEach(emp -> emp.setDepartment(department));
		}
		Date now = new Date();
		department.setCreated(now);
		department.setLastUpdated(now);
		entityManager.persist(department);
		return department;
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department get(final int id) {
		Department dept = entityManager.find(Department.class, Integer.valueOf(id));
		if (dept == null) {
			throw new EntityNotFoundException("Department not found for id " + id);
		}
		return dept;
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
		department.setLastUpdated(new Date());
		return entityManager.merge(department);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void remove(final int id) {
		Department dept = entityManager.find(Department.class, Integer.valueOf(id));
		if (dept == null) {
			throw new EntityNotFoundException("Department not found for id " + id);
		}
		entityManager.remove(dept);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void addEmploye(int departmentId, Employee employee) {
		Department dept = entityManager.find(Department.class, Integer.valueOf(departmentId));
		if (dept == null) {
			throw new EntityNotFoundException("Department not found for id " + departmentId);
		}
		employee.setDepartment(dept);
		entityManager.persist(employee);
		dept.getEmployees().add(employee);
		dept.setLastUpdated(new Date());
		// FIXME Do I need to do this?
		//entityManager.merge(dept);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void removeEmployee(int departmentId, int employeeId) {
		Department dept = entityManager.find(Department.class, Integer.valueOf(departmentId));
		if (dept == null) {
			throw new EntityNotFoundException("Department not found for id " + departmentId);
		}
		Optional<Employee> opt_emp = dept.getEmployees().stream().filter(emp -> emp.getId().intValue() == employeeId)
				.findFirst();
		Employee emp = opt_emp.orElseThrow(() -> new EntityNotFoundException(
				"No such Employee with id " + employeeId + " in department " + departmentId));
		emp.setDepartment(null);
		dept.getEmployees().remove(emp);
		dept.setLastUpdated(new Date());
	}
}
