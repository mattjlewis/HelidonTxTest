package org.test.tx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.test.tx.model.Department;
import org.test.tx.model.Employee;
import org.test.tx.service.ContainerManagedEmf;
import org.test.tx.service.DepartmentServiceInterface;
import org.test.tx.service.ResourceLocal;

import io.helidon.microprofile.server.Server;

@SuppressWarnings("static-method")
@Dependent
public class HelidonTxTest {
	private static final String USER_USERNAME = "user1";
	private static final String USER_PASSWORD = "password";
	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "password";

	private static SeContainer cdiContainer;
	private static Server server;

	@PersistenceUnit(unitName = "HelidonTxTestPuJta")
	private EntityManagerFactory entityManagerFactoryJta;
	@PersistenceUnit(unitName = "HelidonTxTestPuLocal")
	private EntityManagerFactory entityManagerFactoryResourceLocal;
	@Inject
	private DepartmentServiceInterface departmentServiceContainerManagedJta;
	@Inject
	@ContainerManagedEmf
	private DepartmentServiceInterface departmentServiceContainerManagedEmfJta;
	@Inject
	@ResourceLocal
	private DepartmentServiceInterface departmentServiceAppManagedResourceLocal;
	//@Inject
	//@RestClient
	//private DepartmentResourceInterface restClient;

	private static enum DepartmentServiceType {
		CONTAINER_MANAGED_JTA, CONTAINER_MANAGED_EMF_JTA, APP_MANAGED_RESOURCE_LOCAL;
	}

	private static String createHttpBasicAuthToken(String username, String password) {
		return "Basic "
				+ Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
	}

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

	static HelidonTxTest getSelf() {
		return cdiContainer.select(HelidonTxTest.class).get();
	}

	static EntityManagerFactory getEntityManagerFactoryResourceLocal() {
		return getSelf().entityManagerFactoryResourceLocal;
	}

	static EntityManagerFactory getEntityManagerFactoryJta() {
		return getSelf().entityManagerFactoryJta;
	}

	static DepartmentServiceInterface getDepartmentService(DepartmentServiceType type) {
		DepartmentServiceInterface ds = null;
		var self = getSelf();
		switch (type) {
		case CONTAINER_MANAGED_JTA:
			ds = self.departmentServiceContainerManagedJta;
			break;
		case CONTAINER_MANAGED_EMF_JTA:
			ds = self.departmentServiceContainerManagedEmfJta;
			break;
		case APP_MANAGED_RESOURCE_LOCAL:
			ds = self.departmentServiceAppManagedResourceLocal;
			break;
		}

		return ds;
	}

	@AfterEach()
	public void clearData() {
		EntityManager em = getEntityManagerFactoryResourceLocal().createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		int deleted_count = em.createQuery("DELETE FROM Employee").executeUpdate();
		tx.commit();
		System.out.println("Deleted " + deleted_count + " employees");

		tx = em.getTransaction();
		tx.begin();
		deleted_count = em.createQuery("DELETE FROM Department").executeUpdate();
		tx.commit();
		System.out.println("Deleted " + deleted_count + " departments");

		em.close();
	}

	@Test
	public void restClientSecurityTest() {
		Client client = ClientBuilder.newClient();
		WebTarget root = client.target("http://" + server.host() + ":" + server.port()).path("rest");

		try (Response response = root.path("protected").request(MediaType.TEXT_HTML).get()) {
			if (response.getStatus() != Response.Status.UNAUTHORIZED.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
		}

		try (Response response = root.path("protected").request(MediaType.TEXT_HTML)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
		}

		try (Response response = root.path("protected/admin").request(MediaType.TEXT_HTML)
				.header("Authorization", createHttpBasicAuthToken(ADMIN_USERNAME, ADMIN_PASSWORD)).get()) {
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
		}

		try (Response response = root.path("protected/admin").request(MediaType.TEXT_HTML)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			if (response.getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
		}

		try (Response response = root.path("protected/user").request(MediaType.TEXT_HTML)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
		}
	}

	@Test
	public void restClientDepartmentTest() {
		Client client = ClientBuilder.newClient();
		// Required to use PATCH
		client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, Boolean.TRUE);
		WebTarget root = client.target("http://" + server.host() + ":" + server.port()).path("rest");

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Matt", "matt@test.org", "Coffee"),
				new Employee("Fred", "fred@test.org", "Beer"));
		Department dept = new Department("IT", "London", employees);
		Department created_dept = null;
		try (Response response = root.path("department").request(MediaType.APPLICATION_JSON).post(Entity.json(dept))) {
			if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
			System.out.println("Response location: " + response.getLocation());
			created_dept = response.readEntity(Department.class);
			assertNotNull(created_dept);
			assertNotNull(created_dept.getId());
			assertEquals(dept.getName(), created_dept.getName());
			assertEquals(dept.getLocation(), created_dept.getLocation());
		}

		// Find the department
		Department found_dept = null;
		try {
			found_dept = root.path("department").path(created_dept.getId().toString())
					.request(MediaType.APPLICATION_JSON).get(Department.class);
			assertNotNull(found_dept);
			assertNotNull(found_dept.getId());
			assertEquals(dept.getName(), found_dept.getName());
			assertEquals(employees.size(), found_dept.getEmployees().size());
			assertEquals(dept.getEmployees().size(), found_dept.getEmployees().size());
			assertEquals(1, found_dept.getVersion().intValue());
		} catch (WebApplicationException e) {
			fail("Unexpected response status: " + e.getResponse().getStatus());
			// Here simply to avoid the compiler warning about a potential null pointer access
			return;
		}

		// Update the department
		found_dept.setName(dept.getName() + " - updated");
		try {
			Department updated_dept = root.path("department").path(created_dept.getId().toString())
					.request(MediaType.APPLICATION_JSON)
					.method(HttpMethod.PATCH, Entity.json(found_dept), Department.class);
			assertNotNull(updated_dept);
			assertEquals(dept.getName() + " - updated", updated_dept.getName());
			assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());
		} catch (WebApplicationException e) {
			fail("Unexpected response status: " + e.getResponse().getStatus());
		}

		// Should trigger bean validation failure
		dept = new Department("012345678901234567890123456789", "London");
		try (Response response = root.path("department").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(dept, MediaType.APPLICATION_JSON))) {
			if (response.getStatus() != Response.Status.BAD_REQUEST.getStatusCode()) {
				fail("Unexpected response status: '" + response.getStatus());
			}
		}

		// Should pass bean validation but trigger database constraint violation
		dept = new Department("HR", "Reading",
				Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
						new Employee("Jane", "jane@test.org", "012345678901234567890123456789"),
						new Employee("Freddie", "freddie@test.org", "Tea")));
		try (Response response = root.path("department").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(dept, MediaType.APPLICATION_JSON))) {
			if (response.getStatus() != Response.Status.CONFLICT.getStatusCode()) {
				fail("Unexpected response status '" + response.getStatus());
			}
		}
	}

	@Test
	public void resourceLocalEntityManagerFactoryTest() {
		EntityManagerFactory emf = getEntityManagerFactoryResourceLocal();
		assertNotNull(emf);
		EntityManager em = emf.createEntityManager();
		assertNotNull(em);
		EntityTransaction tx = em.getTransaction();
		assertNotNull(tx);
		em.close();
	}

	@Test
	public void resourceLocalEntityManagerDepartmentTest() {
		EntityManagerFactory emf = getEntityManagerFactoryResourceLocal();
		assertNotNull(emf);

		{
			EntityManager em = null;
			EntityTransaction tx = null;
			// See if we can get a transaction - this will fail if using JTA
			try {
				em = emf.createEntityManager();
				tx = em.getTransaction();
			} catch (IllegalStateException e) {
				System.out.println(
						"IllegalStateException: Skipping entity manager test as you cannot use an entity transaction when using a JTA persistence unit: "
								+ e);
				return;
			} finally {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				if (em != null && em.isOpen()) {
					em.close();
				}
			}
		}

		// Create a new department object with no employees
		Department dept = new Department("dept1");
		Date now = new Date();
		dept.setCreated(now);
		dept.setLastUpdated(now);

		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			// Create it in the database
			tx.begin();
			em.persist(dept);
			tx.commit();
			//em.clear();
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
			assertEquals(1, found_dept.getVersion().intValue());
			//em.clear();
			em.close();
		}

		// Update the department
		Date updated_date = new Date();
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			found_dept.setName(dept.getName() + " - updated");
			found_dept.setLastUpdated(updated_date);
			tx.commit();
			//em.clear();
			em.close();
		}

		// Find the department to validate it was updated
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			assertEquals(dept.getName() + " - updated", found_dept.getName());
			assertEquals(dept.getVersion().intValue() + 1, found_dept.getVersion().intValue());
			assertEquals(updated_date, found_dept.getLastUpdated());
			//em.clear();
			em.close();
		}

		// Cleanup
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			// Remove
			found_dept = em.find(Department.class, id);
			assertNotNull(found_dept);
			assertEquals(id, found_dept.getId());
			em.remove(found_dept);
			tx.commit();
			//em.clear();
			em.close();
		}

		// Check that it has been deleted
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			found_dept = em.find(Department.class, id);
			if (found_dept != null) {
				System.out.format("*** Error: Department #%d should have been deleted! Found dept %d: %s%n", id,
						found_dept.getId(), found_dept.getName());
			}
			assertNull(found_dept);
			//em.clear();
			em.close();
		}

		// Create a department with an employee that breaks database constraints
		List<Employee> employees = Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
				new Employee("Jane", "jane@test.org", "012345678901234567890123456789"),
				new Employee("Freddie", "freddie@test.org", "Tea"));
		Department error_dept = new Department("HR", "Reading", employees);

		// Test that the department was not created
		{
			EntityManager em = emf.createEntityManager();
			assertNotNull(em);
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			try {
				em.persist(error_dept);
				System.out.println("*** error_dept.getId() before commit: " + error_dept.getId());
				tx.commit();
				fail("Create should have failed with a database constraint violation");
			} catch (ConstraintViolationException cve) {
				System.out.println("*** Beans Validation Constraint Error (expected): " + cve);
				cve.getConstraintViolations().forEach(cv -> System.out.println(cv.getMessage()));
				// The transaction won't actualy be active as it is rolled back by the entity manager
				if (tx.isActive()) {
					System.out.println("*** Trying to rollback the active transaction");
					tx.rollback();
				}
			} catch (Exception e) {
				System.out.println("***  Error (expected): " + e);
				// The transaction won't actualy be active as it is rolled back by the entity manager
				if (tx.isActive()) {
					System.out.println("*** Trying to rollback the active transaction");
					tx.rollback();
				}
			} finally {
				//em.clear();
				em.close();
			}
			System.out.println("*** error_dept.getId() after close: " + error_dept.getId());
		}

		// Check that we can't find the HR department
		{
			EntityManager em = emf.createEntityManager();
			var query = em.createNamedQuery("Department.findByName", Department.class);
			query.setParameter("name", error_dept.getName());
			var results = query.getResultList();
			assertEquals(0, results.size());
			//em.clear();
			em.close();
		}
	}

	@Test
	public void departmentWithEmployeesTestContainerManagedJta() {
		departmentWithEmployeesTest(getDepartmentService(DepartmentServiceType.CONTAINER_MANAGED_JTA));
	}

	@Test
	public void departmentWithEmployeesTestContainerManagedEmfJta() {
		departmentWithEmployeesTest(getDepartmentService(DepartmentServiceType.CONTAINER_MANAGED_EMF_JTA));
	}

	@Test
	public void departmentWithEmployeesTestAppManagedResourceLocal() {
		departmentWithEmployeesTest(getDepartmentService(DepartmentServiceType.APP_MANAGED_RESOURCE_LOCAL));
	}

	private void departmentWithEmployeesTest(DepartmentServiceInterface departmentService) {
		assertNotNull(departmentService);
		System.out.println(departmentService.getImplementation());

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Matt", "matt@test.org", "Coffee"),
				new Employee("Fred", "fred@test.org", "Beer"));
		Department dept = new Department("IT", "London", employees);

		// Create it in the database
		Department created_dept = departmentService.create(dept);
		assertNotNull(created_dept);
		Integer id = created_dept.getId();
		assertNotNull(id);
		assertEquals(dept.getName(), created_dept.getName());
		assertEquals(employees.size(), created_dept.getEmployees().size());
		created_dept.getEmployees().forEach(emp -> {
			assertNotNull(emp.getId());
			assertEquals(id, emp.getDepartment().getId());
		});

		// Find the department
		Department found_dept = departmentService.get(id.intValue());
		assertNotNull(found_dept);
		assertNotNull(found_dept.getId());
		assertEquals(dept.getName(), found_dept.getName());
		assertEquals(employees.size(), found_dept.getEmployees().size());
		assertEquals(dept.getEmployees().size(), found_dept.getEmployees().size());
		assertEquals(1, found_dept.getVersion().intValue());

		// Update the department
		found_dept.setName(dept.getName() + " - updated");
		Department updated_dept = departmentService.update(found_dept);
		assertNotNull(updated_dept);
		assertEquals(dept.getName() + " - updated", updated_dept.getName());
		assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());
		updated_dept = departmentService.get(updated_dept.getId().intValue());
		assertNotNull(updated_dept);
		assertEquals(dept.getName() + " - updated", updated_dept.getName());
		assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());

		// Add an employee
		Employee emp = new Employee("Eddie", "eddie@spaniel.org", "Water");
		departmentService.addEmploye(id.intValue(), emp);
		found_dept = departmentService.get(id.intValue());
		assertEquals(dept.getEmployees().size() + 1, found_dept.getEmployees().size());

		// Cleanup
		departmentService.remove(id.intValue());

		// Validate that the department was removed
		try {
			found_dept = departmentService.get(id.intValue());
			System.out.println("*** Error: Shouldn't have been able to find department with " + id + ": found "
					+ found_dept.getId() + " - " + found_dept.getName());
			fail("Shouldn't have been able to find department with " + id);
		} catch (EntityNotFoundException e) {
			// Expected
		}
	}

	@Test
	public void departmentWithEmployeesErrorTestContainerManagedJta() {
		departmentWithEmployeesErrorTest(getDepartmentService(DepartmentServiceType.CONTAINER_MANAGED_JTA));
	}

	@Test
	public void departmentWithEmployeesErrorTestContainerManagedEmfJta() {
		departmentWithEmployeesErrorTest(getDepartmentService(DepartmentServiceType.CONTAINER_MANAGED_EMF_JTA));
	}

	@Test
	public void departmentWithEmployeesErrorTestAppManagedResourceLocal() {
		departmentWithEmployeesErrorTest(getDepartmentService(DepartmentServiceType.APP_MANAGED_RESOURCE_LOCAL));
	}

	private void departmentWithEmployeesErrorTest(DepartmentServiceInterface departmentService) {
		assertNotNull(departmentService);

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
				new Employee("Jane", "jane@test.org", "0123456789012345678901234"),
				new Employee("Freddie", "freddie@test.org", "Tea"));
		Department dept = new Department("HR", "Reading", employees);

		// Attempt to create it in the database - this will fail
		try {
			Department created_dept = departmentService.create(dept);
			System.out.println("*** Got dept '" + created_dept.getName() + "' when it shouldn't have been created");
			fail("Create should have failed");
		} catch (Exception e) {
			System.out.println("*** departmentWithEmployeesErrorTest - Error: " + e);
			try {
				Department found_dept = departmentService.findByName(dept.getName());
				System.out.println("*** Error: Found department with name '" + found_dept.getName() + "'");
				fail("Should not have been able to find department with name '" + dept.getName() + "' but did");
			} catch (NoResultException | EntityNotFoundException nfe) {
				// Expected
			}
		}
	}
}
