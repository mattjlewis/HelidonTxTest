package org.test.tx.rest;

import java.net.URI;

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

import org.test.tx.model.Department;
import org.test.tx.service.DepartmentService;

@Path("department")
@Produces(MediaType.APPLICATION_JSON)
public class DepartmentResource {
	@Inject
	private DepartmentService departmentService;

	private static URI createLocation(UriInfo uriInfo, Department department) {
		return uriInfo.getAbsolutePathBuilder().path(department.getId().toString()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(@Context UriInfo uriInfo, Department department) {
		try {
			Department dept = departmentService.create(department);
			return Response.created(createLocation(uriInfo, department)).entity(dept).build();
		} catch (Exception e) {
			return Response.serverError().entity("Error creating department: " + e).build();
		}
	}

	@GET
	public Response get(@PathParam("id") int id) {
		try {
			return Response.ok(departmentService.get(id)).build();
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(Integer.valueOf(id)).build();
		}
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@Context UriInfo uriInfo, Department department) {
		try {
			Department dept = departmentService.update(department);
			return Response.ok(dept).location(createLocation(uriInfo, department)).build();
		} catch (Exception e) {
			return Response.serverError().entity("Error updating department " + department.getId() + ": " + e).build();
		}
	}
}
