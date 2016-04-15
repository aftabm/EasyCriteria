package org.easy.test.criteria.model.generated;

import java.util.Date;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Person.class)
public abstract class Person_
{

	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SetAttribute<Person, Course> courses;
	public static volatile SetAttribute<Person, CourseSession> courseSessions;
	public static volatile SingularAttribute<Person, String> name;
	public static volatile SingularAttribute<Person, PersonType> personType;
	public static volatile SingularAttribute<Person, Date> lastUpdateDateTime;
	public static volatile SingularAttribute<Person, Long> lastUpdateBy;

}
