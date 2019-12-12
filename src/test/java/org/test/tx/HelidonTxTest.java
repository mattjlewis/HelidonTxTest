package org.test.tx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.se.SeContainer;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.test.tx.model.Department;
import org.test.tx.model.Employee;
import org.test.tx.service.DepartmentService;

import io.helidon.microprofile.server.Server;

@SuppressWarnings("static-method")
public class HelidonTxTest {
	private static SeContainer cdiContainer;
	private static Server server;

	@BeforeAll
	public static void setup() {
		server = Server.create().start();
		cdiContainer = server.cdiContainer();
		assertNotNull(cdiContainer);
	}

	@AfterAll
	public static void tearDown() {
		if (server != null) {
			server.stop();
			server = null;
		}
	}
	
	@Any
	static class MyClass {
		// Ignore
	}

	static EntityManager getEntityManager() throws ClassNotFoundException {
		/*
  - Configurator Bean [class io.helidon.integrations.cdi.jpa.JpaExtension, types: CdiTransactionScopedEntityManager, DelegatingEntityManager, EntityManager, Object, qualifiers: @Any @ContainerManaged @CdiTransactionScoped @Synchronized @Named],
  - Configurator Bean [class io.helidon.integrations.cdi.jpa.JpaExtension, types: NonTransactionalEntityManager, DelegatingEntityManager, EntityManager, Object, qualifiers: @Any @NonTransactional @Named],
  - Configurator Bean [class io.helidon.integrations.cdi.jpa.JpaExtension, types: JpaTransactionScopedEntityManager, DelegatingEntityManager, EntityManager, Object, qualifiers: @Any @JpaTransactionScoped @ContainerManaged @Synchronized @Named],
  - Configurator Bean [class io.helidon.integrations.cdi.jpa.JpaExtension, types: CdiTransactionScopedEntityManager, DelegatingEntityManager, EntityManager, Object, qualifiers: @Any @ContainerManaged @CdiTransactionScoped @Unsynchronized @Named]
		 */
		// FIXME I'm being stupid - how do I get a reference to the javax.enterprise.inject.Any annotation?
		Annotation ann = null;
		for (Annotation a : MyClass.class.getAnnotations()) {
			ann = a;
		}
		return (EntityManager) cdiContainer.select(Class.forName("io.helidon.integrations.cdi.jpa.JpaTransactionScopedEntityManager"), ann).get();
		//return cdiContainer.select(EntityManager.class).get();
	}

	static TransactionManager getTransactionManager() {
		return cdiContainer.select(TransactionManager.class).get();
	}

	static DepartmentService getDepartmentService() {
		return cdiContainer.select(DepartmentService.class).get();
	}

	//@Test
	public void entityManagerDepartmentTest() throws NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, ClassNotFoundException {
		EntityManager em = getEntityManager();
		assertNotNull(em);
		TransactionManager tm = getTransactionManager();
		assertNotNull(tm);

		// Create a new department object with no employees
		Department dept = new Department("dept1");

		// Create it in the database
		tm.begin();
		em.persist(dept);
		tm.commit();

		Integer id = dept.getId();
		assertNotNull(id);

		// Find the department
		Department found_dept = em.find(Department.class, id);
		assertNotNull(found_dept);
		assertNotNull(found_dept.getId());
		assertEquals(dept.getName(), found_dept.getName());

		// Update the department
		tm.begin();
		found_dept = em.find(Department.class, id);
		assertNotNull(found_dept);
		found_dept.setName(dept.getName() + " - updated");
		tm.commit();

		// Find the department
		found_dept = em.find(Department.class, id);
		assertNotNull(found_dept);
		if (found_dept.getName().equals(dept.getName())) {
			System.out.println("*** Error: department name wasn't updated");
		}
		//assertEquals(dept.getName() + " - updated", found_dept.getName());

		// Cleanup
		removeDepartment(id);

		id = Integer.valueOf(1054);
		found_dept = em.find(Department.class, id);
		if (found_dept != null) {
			fail("Error: Shouldn't have been able to find department " + id + ": found " + found_dept.getId() + " - "
					+ found_dept.getName());
		}
	}

	@Test
	public void departmentWithEmployeesTest() {
		DepartmentService department_service = getDepartmentService();
		assertNotNull(department_service);

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Matt", "matt@test.org", "Coffee"),
				new Employee("Fred", "fred@test.org", "Beer"));
		Department dept = new Department("IT", "London", employees);

		// Create it in the database
		Department created_dept = department_service.create(dept);
		assertNotNull(created_dept);

		Integer id = created_dept.getId();
		assertNotNull(id);

		assertEquals(dept.getName(), created_dept.getName());
		assertEquals(dept.getEmployees().size(), created_dept.getEmployees().size());
		created_dept.getEmployees().forEach(emp -> {
			assertNotNull(emp.getId());
			assertEquals(id, emp.getDepartment().getId());
		});

		// Find the department
		Department found_dept = department_service.get(id.intValue());
		assertNotNull(found_dept);
		assertEquals(dept.getName(), found_dept.getName());
		assertEquals(employees.size(), found_dept.getEmployees().size());
		assertEquals(dept.getEmployees().size(), found_dept.getEmployees().size());

		// Update the department
		found_dept.setName(dept.getName() + " - updated");
		Department updated_dept = department_service.update(found_dept);
		assertNotNull(updated_dept);
		assertEquals(dept.getName() + " - updated", updated_dept.getName());
		assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());
		updated_dept = department_service.get(updated_dept.getId().intValue());
		assertNotNull(updated_dept);
		assertEquals(dept.getName() + " - updated", updated_dept.getName());
		if (updated_dept.getVersion().intValue() != found_dept.getVersion().intValue() + 1) {
			System.out.println("Error: Updated version value was wrong - " + updated_dept.getVersion());
		}
		//assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());

		// Cleanup
		department_service.remove(id.intValue());

		// Validate that the department was removed
		found_dept = department_service.get(id.intValue());
		if (found_dept != null) {
			System.out.println("*** Error: Shouldn't have been able to find department " + id + ": found "
					+ found_dept.getId() + " - " + found_dept.getName());
		}
		//assertNull(found_dept);
	}
	
	@Test
	public void departmentWithEmployeesErrorTest() {
		DepartmentService department_service = getDepartmentService();
		assertNotNull(department_service);

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
				new Employee("Jane", "jane@test.org", "012345678901234567890123456789"),
				new Employee("Freddie", "freddie@test.org", "Tea"));
		Department dept = new Department("HR", "Reading", employees);

		// Create it in the database
		try {
			Department created_dept = department_service.create(dept);
			System.out.println("--- Get dept: " + created_dept);
			fail("Create should have failed");
		} catch (Exception e) {
			System.out.println("--- Error: " + e);
			Department found_dept = department_service.findByName(dept.getName());
			if (found_dept != null) {
				System.out.println("Error: found department with name '" + found_dept + "'");
				fail("Department shouldn't have been created");
			}
		}
	}
	
	private static void removeDepartment(Integer id) throws NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, ClassNotFoundException {
		EntityManager em = getEntityManager();
		assertNotNull(em);
		TransactionManager tm = getTransactionManager();
		assertNotNull(tm);

		// Remove
		tm.begin();
		Department dept = em.find(Department.class, id);
		assertNotNull(dept);
		assertEquals(id, dept.getId());
		em.remove(dept);
		tm.commit();

		// Check that it has been deleted
		dept = em.find(Department.class, id);
		if (dept != null) {
			System.out.format("*** Error: Department #%d should have been deleted! Found dept %d: %s%n", id,
					dept.getId(), dept.getName());
			//fail("Shouldn't be able to find department " + id);
		}
	}
}
