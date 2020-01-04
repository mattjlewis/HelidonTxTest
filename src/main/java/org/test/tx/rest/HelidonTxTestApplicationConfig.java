package org.test.tx.rest;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;
import org.test.tx.rest.exception.EntityNotFoundMapper;
import org.test.tx.rest.exception.NoResultMapper;
import org.test.tx.rest.exception.PersistenceExceptionMapper;
import org.test.tx.rest.exception.TransactionRollbackMapper;

@ApplicationScoped
@ApplicationPath("/rest")
@LoginConfig(authMethod = "MP-JWT")
public class HelidonTxTestApplicationConfig extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return Set.of(DepartmentResource.class, ProtectedResource.class, EntityNotFoundMapper.class, NoResultMapper.class,
				PersistenceExceptionMapper.class, TransactionRollbackMapper.class);
	}
}
