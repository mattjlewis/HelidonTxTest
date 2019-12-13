# Helidon Transaction Test

Simple project to test Helidon transactions when using JPA.

There is a useful [stackoverflow discussion](https://stackoverflow.com/questions/2360764/ejb3-transaction-rollback/2362881) on the Container Manager Transaction rollback logic that is implemented in EJB3.

1. If you throw your exception with @ApplicationException(rollback=true), you don't have to rollback the transaction manually. Context.setRollbackOnly() forces the container to rollback the transaction, also if there is no exception.
1. A checked exception itself doesn't rollback a transaction. It needs to have the annotation @ApplicationException(rollback=true). If the exception is a RuntimeException and the exception isn't caught, it forces the container to rollback the transaction. But watch out, the container will in this case discard the EJB instance.
1. As mentioned in 2.), if you throw a RuntimeException, the transaction will be rolled back automatically. If you catch an checked exception inside the code, you have to use setRollbackOnly to rollback the transaction.

Useful articles:

1. [Tackling RESOURCE_LOCAL vs. JTA Under Java EE Umbrella and Payara Server](https://dzone.com/articles/resource-local-vs-jta-transaction-types-and-payara)
