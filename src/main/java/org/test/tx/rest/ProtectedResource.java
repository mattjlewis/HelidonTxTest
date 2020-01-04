package org.test.tx.rest;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.helidon.security.Grant;
import io.helidon.security.SecurityContext;
import io.helidon.security.Subject;
import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;

@ApplicationScoped
@Path("protected")
@SuppressWarnings("static-method")
@Authenticated
@Produces(MediaType.TEXT_HTML)
public class ProtectedResource {
	@GET
	public String securityContextTest(@Context SecurityContext context) {
		Subject sub = context.user().get();
		var grants = sub.grants(Grant.class);
		if (grants == null || grants.isEmpty()) {
			System.out.println("No grants for class " + Grant.class);
		} else {
			System.out.println("Grants for Grant.class:");
			grants.forEach(System.out::println);
		}
		return "<html><body><h1>Protected Resource</h1>"
				+ "<h2>Security Context</h2>"
				+ "<ul>"
				+ "<li>Id: " + context.id() + "</li>"
				+ "<li>Username: " + context.userName() + "</li>"
				+ "<li>Service name: " + context.serviceName() + "</li>"
				+ "<li>Has role 'Application/HelidonTxTestSP': " + context.isUserInRole("Application/HelidonTxTestSP") + "</li>"
				+ "<li>Has role 'Application/user': " + context.isUserInRole("Application/user") + "</li>"
				+ "<li>Has role 'Internal/everyone': " + context.isUserInRole("Internal/everyone") + "</li>"
				+ "<li>Has role 'HelidonTxTestUser': " + context.isUserInRole("HelidonTxTestUser") + "</li>"
				+ "<li>Has role 'admin': " + context.isUserInRole("admin") + "</li>"
				+ "<li>Has role 'user': " + context.isUserInRole("user") + "</li>"
				+ "<li>User Subject: " + context.user().get() + "</li>"
				+ "<li>User Principle: " + context.userPrincipal().get() + "</li>"
				+ "<li>ABAC Attribute Names: " + context.userPrincipal().get().abacAttributeNames() + "</li>"
				+ "</ul>"
				+ "<h2>Links</h2>"
				+ "<ul>"
				+ "<li><a href=\"protected/applicationHelidon\">Application/HelidonTxTestSP</a></li>"
				+ "<li><a href=\"protected/applicationUser\">Application/user</a></li>"
				+ "<li><a href=\"protected/everyone\">Internal/everyone</a></li>"
				+ "<li><a href=\"protected/helidonTxTestUser\">HelidonTxTestUser</a></li>"
				+ "<li><a href=\"protected/admin\">admin</a></li>"
				+ "<li><a href=\"protected/user\">user</a></li>"
				+ "</ul>"
				+ "</body></html>";
	}

	@GET
	@Path("applicationHelidon")
	@Authorized
	@RolesAllowed("Application/HelidonTxTestSP")
	public String applicationHelidon(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}

	@GET
	@Path("applicationUser")
	@Authorized
	@RolesAllowed("Application/user")
	public String applicationUser(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}

	@GET
	@Path("everyone")
	@Authorized
	@RolesAllowed("Internal/everyone")
	public String everyone(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}

	@GET
	@Path("helidonTxTestUser")
	@Authorized
	@RolesAllowed("HelidonTxTestUser")
	public String helidonTxTestUser(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}
	
	@GET
	@Path("admin")
	@RolesAllowed("admin")
	public String adminRole(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}

	@GET
	@Path("user")
	@Authorized
	@RolesAllowed("user")
	public String userRole(@Context SecurityContext context) {
		return "<html><body><h1>Authorised</h1><p>User: " + context.userName() + "</p></body></html>";
	}
}
