package org.test.tx.rest;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.test.tx.model.Department;
import org.test.tx.model.Employee;
import org.test.tx.service.DepartmentServiceInterface;

@Path("department")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class DepartmentResource {
	@Context
	private UriInfo uriInfo;
	@Inject
	private DepartmentServiceInterface departmentService;

	private static URI createLocation(UriInfo uriInfo, Department department) {
		return uriInfo.getAbsolutePathBuilder().path(department.getId().toString()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create a new department")
	@APIResponse(description = "The created department", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Department.class)))
	public Response create(@Valid Department department) {
		var employees = department.getEmployees();
		if (employees != null && !employees.isEmpty()) {
			employees.forEach(emp -> {
				if (emp.getFavouriteDrink() != null && emp.getFavouriteDrink().length() > 20) {
					System.out.println("*** Length of favourite drink: " + emp.getFavouriteDrink().length());
					if (emp.getFavouriteDrink().length() > 30) {
						System.out.println("*** Error: length of favourite drink (" + emp.getFavouriteDrink().length()
								+ ") is greater than 30");
					}
				}
			});
		}

		Department dept = departmentService.create(department);
		return Response.created(createLocation(uriInfo, dept)).entity(dept).build();
	}

	@GET
	@Path("{id}")
	@Operation(summary = "Get a specific department")
	@APIResponse(description = "The department instance", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Department.class)))
	public Response get(@PathParam("id") int id) {
		return Response.ok(departmentService.get(id)).build();
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update a department")
	@APIResponse(description = "The department instance", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Department.class)))
	public Response update(Department department) {
		Department dept = departmentService.update(department);
		return Response.ok(dept).location(createLocation(uriInfo, department)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{id}/employee")
	public Response addEmploye(@PathParam("id") int departmentId, Employee employee) {
		departmentService.addEmploye(departmentId, employee);
		return Response.noContent().build();
	}

	@DELETE
	@Path("{did}/employee/{eid}")
	public Response removeEmployee(@PathParam("did") int departmentId, @PathParam("eid") int employeeId) {
		departmentService.removeEmployee(departmentId, employeeId);
		return Response.noContent().build();
	}
}
