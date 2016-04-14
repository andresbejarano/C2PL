+------------------------------------------+
| C2PL - Centralized Two-Phase Locking     |
| By: Andres Bejarano <abejara@purdue.edu> |
+------------------------------------------+

C2PL uses the JDBC library for SQLite 3:

	https://bitbucket.org/xerial/sqlite-jdbc
	https://bitbucket.org/xerial/sqlite-jdbc/downloads

C2PL bases its functionality on Java RMI:

	https://docs.oracle.com/javase/tutorial/rmi/overview.html

	
+-----------------------------------+
| Opening and Compiling the Project |
+-----------------------------------+

The folder structure belongs to an Eclipse project. Open Eclipse and import the project into your
desired workspace. Classes are compiled everytime a change in the code is saved.

IMPORTANT: Do not remove the content in the lib, policies and transactions folders. Those are 
required for the correct functionality of the project.

It is not required for both Central Site and Data Sites to run in the same machine. However, both 
codes are included in the same project for convenience. Still, they do not share more than the
common classes (src/common folder) and the interfaces required for Java RMI.
	
	
+--------------------------+
| Running the Central Site |
+--------------------------+

1) Open a terminal console and go to the C2PL folder.

2) Start the RMI Registry:

	start rmiregistry -J-Djava.rmi.server.codebase=file:bin\
	
	
3) Start the Central Site object:

	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy -Djava.rmi.server.hostname=192.168.1.6 centralsite.CentralSite 45
or	
	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.security.policy=policies\server.policy centralsite.CentralSite 45

Note:
- 45 is the port where the Central Site receives the transactions from the Data Sites. It could be
  any other value. However, it must be the same for the data sites.


+-----------------------+
| Running the Data Site |
+-----------------------+

1) Open a terminal console and go to the C2PL folder.

2) Start the Data Site object:

	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite 192.168.1.6 45 transactions/transactions_long_1.txt
or
	java -classpath bin;lib\sqlite-jdbc-3.8.11.2.jar -Djava.rmi.server.codebase=file:bin\ -Djava.security.policy=policies\client.policy datasite.DataSite localhost 45 transactions/transactions_long_1.txt

Note:
- 192.168.1.6 is the host address (Central Site address).
- 45 is the port where the Central Site is receiving the transactions. Data Sites send transactions 
  to this port.
- transactions/transactions_few_1.txt is the file containing the transactions to be executed in the 
  site.
