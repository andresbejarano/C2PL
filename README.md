# C2PL - Centralized Two-Phase Locking

C2PL is an implementation of the Centralized Two-Phase Locking protocol applied to distributed database 
systems. It is based on the C2PL algorithms described on Principles of Distributed Database Systems book
by Ozsu and Valduriez.

C2PL uses the JDBC library for SQLite 3:
1. <https://bitbucket.org/xerial/sqlite-jdbc>
2. <https://bitbucket.org/xerial/sqlite-jdbc/downloads>

C2PL bases its functionality on Java RMI:

* <https://docs.oracle.com/javase/tutorial/rmi/overview.html>


### Running the Central Site

Running the central site requires to start the rmiregistry service before running the central site module.

1) Go to the C2PL folder.

2) Start the RMI Registry:

	start rmiregistry -J-Djava.rmi.server.codebase=file:bin\
	
	
3) Start the Central Site object:

	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy -Djava.rmi.server.hostname=192.168.1.6 centralsite.CentralSite 45 1000
	
or	
	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy centralsite.CentralSite 45 1000 true

Note:
- 45 is the port where the Central Site receives the transactions from the Data Sites.



### Running the Data Site

1) Go to the C2PL folder.

2) Start the Data Site object:

	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite 192.168.1.6 45 transactions/transactions_many_1.txt
	
or
	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite localhost 45 transactions/transactions_many_1.txt

Note:
- 192.168.1.6 is the host address (Central Site address).
- 45 is the port where the Central Site is receiving the transactions. Data Sites send transactions to this port
- transactions/transactions_few_1.txt is the file containing the transactions to be executed in the site
