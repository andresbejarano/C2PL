package datasite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataManager {
	
	/* The tag name of the class */
	public static final String TAG = DataManager.class.getName();
	
	/* JDBC driver for SQLite */
	public static final String DB_JDBC = "org.sqlite.JDBC";
	
	/* URL to the data base in SQLite using JDBC */
	public static final String DB_URL = "jdbc:sqlite:c2pl.db";
	
	/**
	 * Creates the database. If the database could be created then it
	 * return true, otherwise returns false. If false then it could be
	 * that there was not connection to the database or the database
	 * already exist
	 * @return
	 */
	public static int create() {
		
		/* The connection object to the database */
		Connection connection = null;
		
		/* The result code after executing the query in the database */
		int result = 0;
		
		/* Try to connect to the database and execute the query */
		try {
			
			/* Connect to the database using JDBC */
			Class.forName(DB_JDBC);
			connection = DriverManager.getConnection(DB_URL);
			
			/* Generate the statement */
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			/* Drop any previous table */
			statement.executeUpdate("drop table if exists items");
			
			/* Create the table */
			String query = "create table items (item string, value integer)";
			result = statement.executeUpdate(query);
			
			/* Close the statement and the connection to the database */
			statement.close();
			connection.close();
			
		}
		catch(ClassNotFoundException e) {
			System.err.println(TAG + " Class Not Found Exception: " + e.getMessage());
		}
		catch(SQLException e) {
			System.err.println(TAG + " SQL Exception: " + e.getMessage());
		}
		
		/* Return the result from the database */
		return result;
	}
	
	/**
	 * Returns the value of the given item in the database
	 * @param item
	 * @return
	 */
	public static int read(String item) {
		
		/* The connection object to the database */
		Connection connection = null;
		
		/* The value of the item */
		int value = 0;
		
		/* Try to connect to the database */
		try {
			
			/* Connect to the database using JDBC */
			Class.forName(DB_JDBC);
			connection = DriverManager.getConnection(DB_URL);
			
			/* Generate the statement */
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			String query = "select value from items where item = '" + item + "'";
			
			/* Execute the statement */
			ResultSet result = statement.executeQuery(query);
			
			/* If there is a result then get the value */
			if(result.next()) {
				value = result.getInt("value");
			}
			
			/* Close the result, the statement and the connection */
			result.close();
			statement.close();
			connection.close();
			
		}
		catch(ClassNotFoundException e) {
			System.err.println(TAG + " Class Not Found Exception: " + e.getMessage());
		}
		catch(SQLException e) {
			System.err.println(TAG + " SQL Exception: " + e.getMessage());
		}
		
		/* Return the value from the database */
		return value;
	}
	
	/**
	 * Shows in console all the elements in the table
	 */
	public static void show() {
		
		/* The connection object to the database */
		Connection connection = null;
		
		/* The data item */
		String item;
		
		/* The value of the item */
		int value = 0;
		
		/* Try to connect to the database */
		try {
			
			/* Connect to the database using JDBC */
			Class.forName(DB_JDBC);
			connection = DriverManager.getConnection(DB_URL);
			
			/* Generate the statement */
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			String query = "select * from items";
			
			/* Execute the statement */
			ResultSet result = statement.executeQuery(query);
			
			/* If there is a result then get the value */
			while(result.next()) {
				item = result.getString("item");
				value = result.getInt("value");
				System.out.println("[" + item + ": " + value + "]");
			}
			
			/* Close the result, the statement and the connection */
			result.close();
			statement.close();
			connection.close();
			
		}
		catch(ClassNotFoundException e) {
			System.err.println(TAG + " Class Not Found Exception: " + e.getMessage());
		}
		catch(SQLException e) {
			System.err.println(TAG + " SQL Exception: " + e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * @param item
	 * @param value
	 * @return
	 */
	public static int write(String item, int value) {
		
		/* The connection object to the database */
		Connection connection = null;
		
		/* The result code after executing the query in the database */
		int result = 0;
		
		/* Try to connect to the database and execute the query */
		try {
			
			/* Connect to the database using JDBC */
			Class.forName(DB_JDBC);
			connection = DriverManager.getConnection(DB_URL);
			
			/* Generate the statement */
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			/* First check if the item already exists in the table */
			String query = "select value from items where item = '" + item + "'";
			ResultSet resultset = statement.executeQuery(query);
			
			/* If there is a next row then the item already exists in the table, otherwise it doesn't.
			 * Update the query based on the situation */
			if(resultset.next()) {
				query = "update items set value = " + value + " where item = '" + item + "'";
				//System.out.println("item " + item + " already exists");
			}
			else {
				query = "insert into items values ('" + item + "', " + value + ")";
				//System.out.println("item " + item + " doesn't exist");
			}
			
			/* Execute the statement */
			result = statement.executeUpdate(query);
			
			/* Close the statement and the connection */
			statement.close();
			connection.close();
			
		}
		catch(ClassNotFoundException e) {
			System.err.println(TAG + " Class Not Found Exception: " + e.getMessage());
		}
		catch(SQLException e) {
			System.err.println(TAG + " SQL Exception: " + e.getMessage());
		}
		
		/* Return the result from the database */
		return result;
	}

}
