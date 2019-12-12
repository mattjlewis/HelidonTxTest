package org.test.tx.rest;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Application;

@ApplicationScoped
//@ApplicationPath("helidontxtest")
public class HelidonTxTestApplicationConfig extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			return Set.of(DepartmentResource.class);
	}
}
