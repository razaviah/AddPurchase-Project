// Student's Name: Amirhossein Razavi
// Student ID: ahr91
// Student Number: 216715963

import java.util.*;
import java.net.*;
import java.text.*;
import java.time.LocalDate;
import java.lang.*;
import java.io.*;
import java.sql.*;
import pgpass.*;
import java.time.LocalDateTime;

public class AddPurchase {
private String userName = "ahr91";
private Connection conDB;   // Connection to the database system.
private String url;         // URL: Which database?

private Integer cid;    
private String  club;   
private String title;
private Integer year;
private Timestamp whenp;
private Integer qnty;
private String user;

// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public AddPurchase (String[] args) {


// Set up the DB connection.
        try {
            // Register the driver with DriverManager.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        url = "jdbc:postgresql://db:5432/ahr91";

        // set up acct info
        // fetch the PASSWD from <.pgpass>
        Properties props = new Properties();
        try {
            String passwd = PgPass.get("db", "*", userName, userName);
            props.setProperty("user",    "ahr91");
            props.setProperty("password", passwd);
            
        } catch(PgPassException e) {
            System.out.print("\nCould not obtain PASSWD from <.pgpass>.\n");
            System.out.println(e.toString());
            System.exit(0);
        }

        // Initialize the connection.
        try {
            // Connect with a fall-thru id & password
            conDB = DriverManager.getConnection(url, props);
        } catch(SQLException e) {
            System.out.print("\nSQL: database connection error.\n");
            System.out.println(e.toString());
            System.exit(0);
        }    
        
     // Let's have autocommit turned off.  No particular reason here.
        try {
             conDB.setAutoCommit(false);
        } catch(SQLException e) {
            System.out.print("\nFailed trying to turn autocommit off.\n");
            e.printStackTrace();
            System.exit(0);
        }    
    int len = args.length;
    if (len < 8 || len > 14) {
        System.out.println("Unacceptable Input! The input should be in the following format:");
        System.out.println("-c <cid>  -b <club>  -t <title> -y <year>  [-w <whenp>] [-q <qnty> ] [-u <user> ]");
        System.exit(0);
    } else {
    try {

        for (int i = 0; i < args.length; i = i+2) {
            if (args[i].equals("-c")) { // cid
            	cid = Integer.parseInt(args[i+1]);
            } else if (args[i].equals("-b")) { // club
                club = args[i+1];
            } else if (args[i].equals("-y")) { // year
                year = Integer.parseInt(args[i+1]);
            } else if (args[i].equals("-t")) { // title
                title = args[i+1];
            } else if (args[i].equals("-u")) { // user
                user = args[i+1];
            } else if (args[i].equals("-q")) { // qnty
                qnty = Integer.parseInt(args[i+1]);
            } else if (args[i].equals("-w")) { // whenp
            	if (args[i+1].length()==10) whenp = Timestamp.valueOf(args[i+1] + " 00:00:00");
               else whenp = Timestamp.valueOf(args[i+1]);
            }


        }

        } catch (NumberFormatException e) {
            System.out.println("Input for -w <whenp> or -y <year> or -c <cid> is not valid!");
            System.exit(0);
        } catch (IllegalArgumentException e) {
        	   System.out.println("The values entered are not in the requested format.");
            System.exit(0);
        } catch (Exception e) {
        	   System.out.println("Something is wrong!");
        	   System.out.println(e.toString());
        	   System.exit(0);
        }
    }


	 if (!clubExists() || !bookExists() || !customerExists()){
	 	  System.out.printf("The customer with cid %d, book with title %s and year %d\n", cid, title, year);
	 	  System.exit(0);
	 }
    // check whether the cid belongs to that club
    if (!cidCheck()) {
        System.out.printf("The customer with cid %d does not belong to the club %s\n", cid, club);
        System.exit(0);
    }

    if (!clubOfferBook()) {
        System.out.printf("The club %s does not offer the book %s published in %d \n", club, title, year);
        System.exit(0);
    }

    if (qnty != null && !qntyCheck()) {
        System.out.println("The -q <qnty> value is not positive (<= 0)");
        System.exit(0);
    }

    if (whenp != null && !whenpCheck()) {
        System.out.println("Whenp is not today!");
        System.exit(0);
    }
    
    if (qnty == null) qnty = 1;
    if (whenp == null) whenp = Timestamp.valueOf(LocalDateTime.now());
    if (user == null) user = "ahr91";

    addPurchase();

    // Commit.  Okay, here nothing to commit really, but why not...
    try {
        conDB.commit();
    } catch(SQLException e) {
        System.out.print("\nFailed trying to commit.\n");
        e.printStackTrace();
        System.exit(0);
    }    
    // Close the connection.
    try {
        conDB.close();
    } catch(SQLException e) {
        System.out.print("\nFailed trying to close the connection.\n");
        e.printStackTrace();
        System.exit(0);
    }


    

}


// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public boolean cidCheck() {
	String            queryText = "";     // The SQL text.
	PreparedStatement querySt   = null;   // The query handle.
	ResultSet         answers   = null;   // A cursor.
	
	boolean exists = false;
	queryText =   "SELECT club, cid "
					+ "FROM yrb_member "
					+ "WHERE club = ? AND cid = ?";
	
	
	
	// Prepare the query.
	try {
		querySt = conDB.prepareStatement(queryText);
	} catch(SQLException e) {
		System.out.println("cidCheck() failed in prepare");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Execute the query
	try {
		querySt.setString(1, club);
		querySt.setInt(2, cid.intValue());
		answers = querySt.executeQuery();
	} catch (SQLException e) {
		System.out.println("cidCheck() failed in execute");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Any answer?
	try {
	   if (answers.next()) {
	    exists = true;
	   } else {
	    exists = false;
	
	   }
	} catch(SQLException e) {
		System.out.println("cidCheck() failed in cursor.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Close the cursor.
	try {
		answers.close();
	} catch(SQLException e) {
		System.out.println("cidCheck() failed closing cursor.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// We're done with the handle.
	try {
		querySt.close();
	} catch(SQLException e) {
		System.out.println("cidCheck() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	return exists;

}


// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public boolean clubOfferBook() {
	String            queryText = "";     // The SQL text.
	PreparedStatement querySt   = null;   // The query handle.
	ResultSet         answers   = null;   // A cursor.
	
	boolean offers = false;
	
	queryText =   "SELECT club, title, year "
				+ "FROM yrb_offer "
				+ "WHERE club = ? AND title = ? AND year = ?";
	
	// Prepare the query.
	try {
		querySt = conDB.prepareStatement(queryText);
	} catch(SQLException e) {
		System.out.println("clubOfferBook() failed in prepare.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Execute the query
	try {
		querySt.setString(1, club);
		querySt.setString(2, title);
		querySt.setInt(3, year.intValue());
		answers = querySt.executeQuery();
	} catch (SQLException e) {
		System.out.println("clubOfferBook() failed in execute.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Any answer?
	try {
	   if (answers.next()) {
	    offers = true;
	   } else {
	    offers = false;
	   }
	} catch(SQLException e) {
		System.out.println("clubOfferBook() failed in cursor.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Close the cursor.
	try {
		answers.close();
	} catch(SQLException e) {
		System.out.println("clubOfferBook() failed closing cursor.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// We're done with the handle.
	try {
		querySt.close();
	} catch(SQLException e) {
		System.out.println("clubOfferBook() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	return offers;

}
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public boolean qntyCheck() {
	return qnty > 0;
}
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
public boolean whenpCheck() { 
	//System.out.println(whenp);
	//System.out.println(formatter);
	//System.out.println(LocalDate.now());
	LocalDate ld = LocalDate.parse(whenp.toString().substring(0,10));
	boolean check = ld.equals(LocalDate.now());
	return check;

}
// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
public void addPurchase() {
	String            queryText = "";     // The SQL text.
	PreparedStatement querySt   = null;   // The query handle.
	ResultSet         answers   = null;   // A cursor.
	
	queryText =  "INSERT INTO yrb_purchase(cid, club, title, year, whenp, qnty) VALUES (?, ?, ?, ?, ?, ?)";
	
	// Prepare the query.
	try {
		querySt = conDB.prepareStatement(queryText);
	} catch(SQLException e) {
		System.out.println("addPurchase() failed in prepare.");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// Execute the query
	try {
		querySt.setInt(1, cid.intValue());
		querySt.setString(2, club);
		querySt.setString(3, title);
		querySt.setInt(4, year.intValue());
		querySt.setTimestamp(5, whenp);
		querySt.setInt(6, qnty.intValue());
		querySt.executeUpdate();
		conDB.commit();
	} catch (SQLException e) {
		System.out.println("addPurchase() failed in execute");
		System.out.println(e.toString());
		System.exit(0);
	}
	
	// We're done with the handle.
	try {
		querySt.close();
	} catch(SQLException e) {
		System.out.println("addPurchase() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
	}
}
//	--------------------------------------------------------------------------------------------------------
	public boolean clubExists() {
		String            queryText = "";     // The SQL text.
		PreparedStatement querySt   = null;   // The query handle.
		ResultSet         answers   = null;   // A cursor.

		boolean exists = false;

		queryText = "SELECT club "
					 + "FROM yrb_club "
					 + "WHERE club = ?";

		// Prepare the query.
		try {
		querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
		System.out.println("clubExists() failed in prepare");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Execute the query
		try {
		querySt.setString(1, club);
		answers = querySt.executeQuery();
		} catch (SQLException e) {
		System.out.println("clubExists() failed in execute");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Any answer?
		try {
		   if (answers.next()) {
		    exists = true;
		   } else {
		    exists = false;
		   }
		} catch(SQLException e) {
		System.out.println("clubExists() failed in cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Close the cursor.
		try {
		answers.close();
		} catch(SQLException e) {
		System.out.println("clubExists() failed closing cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// We're done with the handle.
		try {
		querySt.close();
		} catch(SQLException e) {
		System.out.println("clubExists() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
		}

		return exists;
		}
//	--------------------------------------------------------------------------------------------------------
	public boolean bookExists() {
		String            queryText = "";     // The SQL text.
		PreparedStatement querySt   = null;   // The query handle.
		ResultSet         answers   = null;   // A cursor.

		boolean exists = false;

		queryText = "SELECT title,year "
					 + "FROM yrb_book "
					 + "WHERE title = ? AND year=?";

		// Prepare the query.
		try {
		querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
		System.out.println("bookExists() failed in prepare.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Execute the query
		try {
		querySt.setString(1, title);
		querySt.setInt(2, year.intValue());
		answers = querySt.executeQuery();
		} catch (SQLException e) {
		System.out.println("bookExists() failed in execute");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Any answer?
		try {
		   if (answers.next()) {
		    exists = true;
		   } else {
		    exists = false;
		   }
		} catch(SQLException e) {
		System.out.println("bookExists() failed in cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Close the cursor.
		try {
		answers.close();
		} catch(SQLException e) {
		System.out.println("bookExists() failed closing cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// We're done with the handle.
		try {
		querySt.close();
		} catch(SQLException e) {
		System.out.println("bookExists() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
		}

		return exists;
		}
//	--------------------------------------------------------------------------------------------------------
	public boolean customerExists() {
		String            queryText = "";     // The SQL text.
		PreparedStatement querySt   = null;   // The query handle.
		ResultSet         answers   = null;   // A cursor.

		boolean exists = false;

		queryText = "SELECT cid "
					 + "FROM yrb_customer "
					 + "WHERE cid = ?";

		// Prepare the query.
		try {
		querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
		System.out.println("customerExists() failed in prepare");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Execute the query
		try {
		querySt.setInt(1, cid.intValue());
		answers = querySt.executeQuery();
		} catch (SQLException e) {
		System.out.println("customerExists() failed in execute.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Any answer?
		try {
		   if (answers.next()) {
		    exists = true;
		   } else {
		    exists = false;
		   }
		} catch(SQLException e) {
		System.out.println("customerExists() failed in cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// Close the cursor.
		try {
		answers.close();
		} catch(SQLException e) {
		System.out.println("customerExists() failed closing cursor.");
		System.out.println(e.toString());
		System.exit(0);
		}

		// We're done with the handle.
		try {
		querySt.close();
		} catch(SQLException e) {
		System.out.println("customerExists() failed closing the handle.");
		System.out.println(e.toString());
		System.exit(0);
		}

		return exists;
		}
	
public static void main(String[] args) {
	new AddPurchase(args);
}

}