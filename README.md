# AddPurchase-Project

## The Project
In this project, I have written an application program in Java using JDBC that will update the York River Bookstore (YRB) database.
### 1. The Task
The task that the store managers have to do is to add today's purchase records into the database. You have been asked to automate this task with an application program, let us call it AddPurchase. You are to make this in Java using JDBC; so, AddPurchase.java. The app should connect with EECS's PostgreSQL database server at db with the YRB database to record the purchases.
### 2. The Specification
The app will be called from the command line, accepting flags and parameters,

% java AddPurchase -c <cid>  -b <club>  -t <title> -y <year>  [-w <when>] [-q <qnty> ] [-u <user> ]

•	cid(c): the customer id who made the purchase

•	club(b): which club that the puchase is made

•	title(t), year(y): which book the customer purchased

•	whenp(w)(optional): when the purchase is made. if not provided, use the system current time.

•	qnty(q)(optional): the number of copies of the book in this purchase. The default is 1.

•	user(u)(optional): which user and database the app is connecting with and to, respectively. This should default to your user name (which is also your database's name)

Important: flags and parameters come in pairs but may in different order.

### 3. Error Messages:
The app should provide an error message back to the user for each of the following cases. (Your Java program should finish without failing in error itself in these cases!)

•	The customer (cid), the club, or the book (title & year) does not exist: if it does not exist in the corresponding table, the app should state this and not make any changes to the database.

•	The customer (cid) doesn't belong to that club: if the customer is not a member of the given club, the app should state this and not make any changes to the database.


•	The club doesn't offer the book (title & year): if the club does not offer the book, the app should state this and not make any changes to the database.

•	whenp is not today: if the new purchase is not made in today (the day performing your app), the app should state this and not make any changes to the database.

•	qnty value is improper: if the qnty is not a positive integer, the app should state this and not make any changes to the database.

### 4. Result
Given no failure mode occurs, your app should add a tuple into the yrb_purchase table with the specified parameters.

More details can be found at:
[AddPurchase-project.pdf](https://github.com/razaviah/AddPurchase-Project/files/9134571/AddPurchase.project.pdf)
