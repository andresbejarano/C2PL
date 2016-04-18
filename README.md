# C2PL - Centralized Two-Phase Locking

C2PL is an implementation of the Centralized Two-Phase Locking protocol applied to distributed database systems. It is based on the C2PL algorithms described on [Principles of Distributed Database Systems](http://www.springer.com/us/book/9781441988331) book by Ozsu and Valduriez.

The project consists on two main modules:

1. The Central Site module: Corresponds to the implementation of the central site in the system who is in charge of managing locks over the data items. It works along with the Lock Manager which keeps the lock table and gives the instructions for acquiring and releasing locks. This module doesn't have an associated data base.

2. The Data Site module: References any site that runs transactions and require locks for the data items. Every data site requests the locks to the central site. It has associated a local data base which is managed by the Data Manager.

C2PL uses the JDBC library for SQLite 3:

* <https://bitbucket.org/xerial/sqlite-jdbc>
* <https://bitbucket.org/xerial/sqlite-jdbc/downloads>

C2PL bases its functionality on Java RMI:

* <https://docs.oracle.com/javase/tutorial/rmi/overview.html>


### Running the Central Site

Running the central site requires to start the rmiregistry service before running the central site module.

1) Go to the C2PL folder.

2) Start the RMI Registry:

```
start rmiregistry -J-Djava.rmi.server.codebase=file:bin\
```
	
	
3) Start the Central Site object:

```
java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy -Djava.rmi.server.hostname=192.168.1.6 centralsite.CentralSite 45 1000
```

or	

```
java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy centralsite.CentralSite 45 1000 true
```

Notes:
* 45 is the port where the Central Site receives the transactions from the Data Sites.
* 1000 is the time period (in milliseconds) for checking deadlocks in the lock table.



### Running the Data Site

1) Go to the C2PL folder.

2) Start the Data Site object:

```
java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite 192.168.1.6 45 transactions/transactions_many_1.txt
```

or

```
java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite localhost 45 transactions/transactions_many_1.txt
```

Note:
* 192.168.1.6 is the host address (Central Site address).
* 45 is the port where the Central Site is receiving the transactions. Data Sites send transactions to this port
* transactions/transactions_few_1.txt is the file containing the transactions to be executed in the site
