package org.test.tx.rest;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import org.test.tx.service.DepartmentServiceInterface;

@Path("department")
@Produces(MediaType.APPLICATION_JSON)
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
	public Response create(Department department) {
		try {
			Department dept = departmentService.create(department);
			return Response.created(createLocation(uriInfo, department)).entity(dept).build();
		} catch (Exception e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
			return Response.serverError().entity("Error creating department: " + e).build();
		}
	}

	@GET
	@Path("{id}")
	@Operation(summary = "Get a specific department")
	@APIResponse(description = "The department instance", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Department.class)))
	public Response get(@PathParam("id") int id) {
		try {
			Optional<Department> opt_dept = departmentService.get(id);
			if (opt_dept.isPresent()) {
				return Response.ok(opt_dept.get()).build();
			}
			return Response.status(Response.Status.NOT_FOUND).entity(Integer.valueOf(id)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Integer.valueOf(id)).build();
		}
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update a department")
	@APIResponse(description = "The department instance", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Department.class)))
	public Response update(Department department) {
		try {
			Department dept = departmentService.update(department);
			return Response.ok(dept).location(createLocation(uriInfo, department)).build();
		} catch (Exception e) {
			return Response.serverError().entity("Error updating department " + department.getId() + ": " + e).build();
		}
	}
}
