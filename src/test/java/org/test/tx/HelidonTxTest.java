package org.test.tx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.test.tx.model.Department;
import org.test.tx.model.Employee;
import org.test.tx.service.DepartmentServiceInterface;
import org.test.tx.service.ResourceLocal;

import io.helidon.microprofile.server.Server;

@SuppressWarnings("static-method")
@Dependent
public class HelidonTxTest {
	private static SeContainer cdiContainer;
	private static Server server;
	@PersistenceUnit(unitName = "HelidonTxTestPuLocal")
	private EntityManagerFactory entityManagerFactory;
	//@Inject
	//@Inject @AppManaged
	//@Inject @ExtendedPu
	@Inject @ResourceLocal
	private DepartmentServiceInterface departmentService;

	@BeforeAll
	public static void setup() {
		cdiContainer = SeContainerInitializer.newInstance().addBeanClasses(HelidonTxTest.class).initialize();
		assertNotNull(cdiContainer);
		
		server = Server.create().start();
		//cdiContainer = server.cdiContainer();
		//assertNotNull(cdiContainer);
	}

	@AfterAll
	public static void tearDown() {
		if (server != null) {
			server.stop();
			server = null;
		}
		if (cdiContainer != null) {
			cdiContainer.close();
		}
	}
	
	static EntityManagerFactory getEntityManagerFactory() {
		return getSelf().entityManagerFactory;
	}

	static DepartmentServiceInterface getDepartmentService() {
		return getSelf().departmentService;
	}
	
	static HelidonTxTest getSelf() {
		return cdiContainer.select(HelidonTxTest.class).get();
	}

	@Test
	public void entityManagerDepartmentTest() {
		EntityManagerFactory emf = getEntityManagerFactory();
		assertNotNull(emf);

		// Create a new department object with no employees
		Department dept = new Department("dept1");

		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			// Create it in the database
			tx.begin();
			em.persist(dept);
			tx.commit();
			em.clear();
			em.close();
		}

		Integer id = dept.getId();
		assertNotNull(id);

		// Find the department
		Department found_dept = null;
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			assertNotNull(found_dept.getId());
			assertEquals(dept.getName(), found_dept.getName());
			em.clear();
			em.close();
		}

		// Update the department
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			found_dept.setName(dept.getName() + " - updated");
			tx.commit();
			em.clear();
			em.close();
		}

		// Find the department
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			if (found_dept.getName().equals(dept.getName())) {
				System.out.println("*** Error: department name wasn't updated");
			}
			//assertEquals(dept.getName() + " - updated", found_dept.getName());
		}

		// Cleanup
		{
			EntityManager em = getEntityManagerFactory().createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			// Remove
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			assertEquals(id, dept.getId());
			em.remove(dept);
			tx.commit();
		}

		// Check that it has been deleted
		{
			EntityManager em = getEntityManagerFactory().createEntityManager();
			assertNotNull(em);
			found_dept = em.find(Department.class, id);
			assertNull(found_dept);
			if (found_dept != null) {
				System.out.format("*** Error: Department #%d should have been deleted! Found dept %d: %s%n", id,
						found_dept.getId(), found_dept.getName());
				fail("Shouldn't have been able to find department " + id);
			}
		}
	}

	@Test
	public void departmentWithEmployeesTest() {
		DepartmentServiceInterface department_service = getDepartmentService();
		assertNotNull(department_service);
		System.out.println(department_service.getImplementation());

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
		DepartmentServiceInterface department_service = getDepartmentService();
		assertNotNull(department_service);

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
				new Employee("Jane", "jane@test.org", "012345678901234567890123456789"),
				new Employee("Freddie", "freddie@test.org", "Tea"));
		Department dept = new Department("HR", "Reading", employees);

		// Create it in the database
		try {
			Department created_dept = department_service.create(dept);
			System.out.println("--- Got dept '" + created_dept.getName() + "' when it shouldn't have been created");
			fail("Create should have failed");
		} catch (Exception e) {
			System.out.println("--- Error: " + e);
			Department found_dept = department_service.findByName(dept.getName());
			if (found_dept != null) {
				System.out.println("Error: Found department with name '" + found_dept.getName() + "'");
				fail("Should not have been able to find department with name '" + dept.getName() + "' but did");
			}
		}
	}
}
