Tired of writing SQL in Java? Afraid of fragility of named queries?  Want to write metadata based type-safe queries, but lost in verbose JPA api? Then this framework is for you. Now you can now write metadata based, type-safe, debugable, readable, and maintainable queries in Object Oriented way easily. No prior knowledge of SQL or JPA required.
----------- 
HOW TO USE:
-----------
There are two main classes in this framework; 1- CriteriaComposer, 2- CriteriaProcessor

Use CriteriaComposer to construct your query using one or all of the following constructs: Select, Where, 
Group by, Order By, Join, Logical Operators (AND, OR, NOT), Comparison Operator (EQUAL, LIKE, IN, etc..), 
AggregateFunction (SUM, COUNT, AVERAGE, etc..). 

Use CrieriaProcessor to execute this query and get the results. There are two types of API in this class; 1- Entity based, 2- Tuple based

Use Entity based API when you want to get all columns of the root entity. Use Tuple based API when you to 
retrieve selected columns from one or more than one entities. 

----------- 
EXAMPLE 1: SQL : select person.name, sum(course.unit) from person inner join course_session inner join course group by person.name
-----------
CriteriaComposer<Person> studentUnitCount = CriteriaComposer.from(Person.class).select(Person_.name).groupBy(Person_.name);
studentUnitCount.join(Person_.courseSessions).join(CourseSession_.course).select(AggregateFunction.SUM, Course_.unit);		
		
List<Tuple>  result= criteriaProcessor.findAllTuple(studentUnitCount);

for(Tuple tuple: result)
    System.print(tuple.get("Person.name"));
    
Alternatively you can define your own alias e.g. {select(Person_.name, "name")}. This alias should be unique across your criteria, if you select another column with the same alias e.g.{select(Course_.name, "name")} then it would result in an error. 

-----------
EXAMPLE 2: SQL: select * from course where course.last_update_date_time between ? and ?
----------- 
CriteriaComposer<Course> courseCriteria = new CriteriaComposer<Course>(Course.class);
criteria.where(Course_.lastUpdateDateTime, ComparisonOperator.BETWEEN, thatDate, toDate);
 
List<Course> result = criteriaProcessor.findAllEntity(courseCriteria);

------ 	
NOTE: The best way to understand this framework is to review "CriteriaProcessorTest".
------ 

----------- 	
REFERENCES:
-----------
- Mike Keith, et al, Pro JPA 2, Apress, 2009, ISBN 978-1-4302-1956-9. 
- http://www.ibm.com/developerworks/java/library/j-typesafejpa/
- http://docs.jboss.org/hibernate/stable/entitymanager/reference/en/html/querycriteria.html
- http://docs.jboss.org/hibernate/entitymanager/3.5/reference/en/html/listeners.html
- http://openjpa.apache.org/builds/2.0.1/apache-openjpa/docs/jpa_overview_criteria.html
- http://blog.sqlauthority.com/2007/07/04/sql-server-definition-comparison-and-difference-between-having-and-where-clause/
- http://www.w3schools.com/sql/
- http://java.sun.com/j2se/1.5/pdf/generics-tutorial.pdf
- http://download.oracle.com/javaee/6/tutorial/doc/gjivm.html
- http://download.oracle.com/docs/cd/E19798-01/821-1841/gjixa/index.html
- http://download.oracle.com/javaee/6/api/javax/persistence/EntityManager.html
- http://download.oracle.com/javaee/6/api/javax/persistence/criteria/CriteriaQuery.html
- http://download.oracle.com/javaee/6/api/javax/persistence/criteria/CriteriaBuilder.html
- http://eskatos.wordpress.com/2007/10/15/unit-test-jpa-entities-with-in-memory-database/
- http://www.hibernate.org/subprojects/tools.html
- http://agoncal.wordpress.com/2010/05/28/jpa-2-0-criteria-api-with-maven-and-eclipselink/
- http://www.avajava.com/tutorials/lessons/how-do-i-generate-and-deploy-a-javadoc-jar-file-for-my-project.html
- http://www.objectdb.com/java/jpa
------------------------------------------------------------------------------------------------
YOUR LIFE WITHOUT EASY-CRITERIA

Without Easy Criteria framework you have to write following code plus additional code 
for complex queries for each of your query. Each person who need to modify this code also
need to have reasonable expertise  in JPA. As you can see that criteria written in native JPA
API is not cleanly readable and maintainable.
	  	  
With Easy Criteria this can be rewritten in three lines
	  
CriteriaComposer<Person> criteria = CriteriaComposer.from(Person.class).where(Person_.name,EQUAL,"foo").
  join(Person_.Type).where(PersonType_.name, EQUAL, "STUDENT");
	  
   List<Person>  result= criteriaProcessor.findAllEntity(criteria);	   
------------------------------------------------------------------------------------------------	  
	 Note: Following code is for illlustration purpose only, may not be syntactically correct
-------------------------------------------------------------------------------------------------
	  
	CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
	CriteriaQuery<Person> criteria = criteriaBuilder.createQuery(Person.class);				
	criteria.distinct(true);
		
	//create roots
	Root<Person> root = criteria.from(Person.class);		
		
	//create joins
	Join j1 = root.join(Person_.personType);
	Join j2 = j1.join(......);
		.....
		
	Root<PersonType> child = criteria.from(PersonType.class);
		
		......
		
	//creates predicates
	Predicate predicate1 = criteriaBuilder.equal(root.get("name"), "foo");	
	Predicate predicate2 = criteriaBuilder.equal(j2.get("type"), "STUDENT");	
 	.....
		
	//OR
	criteria.where(predicate1, predicate2);		
				
	if (orderBy !=null)
	{
		if (ascending)
		{
			criteriaBuilder.asc(root.get(attribute)));
		}
		else
		{
			orderByList.add(criteriaBuilder.desc(root.get(attribute)));
		}	
	}
		
	TypedQuery<Person> query = em.createQuery(criteria);
				
	//set query properties			
	if (lockMode != null)
	{
		query.setLockMode(lockMode);
	}

	if (maxResult > 0)	
	{
		query.setMaxResults(maxResult);
			
		if (startIndex >=0)
			uery.setFirstResult(startIndex*maxResult);
	}
		
	List<Person> result = query.getResultList();
		
........
	