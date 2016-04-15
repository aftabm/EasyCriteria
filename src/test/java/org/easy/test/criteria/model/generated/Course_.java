package org.easy.test.criteria.model.generated;

import java.util.Date;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Course.class)
public abstract class Course_
{

	public static volatile SingularAttribute<Course, Long> id;
	public static volatile SetAttribute<Course, CourseSession> courseSessions;
	public static volatile SingularAttribute<Course, String> name;
	public static volatile SingularAttribute<Course, Integer> unit;
	public static volatile SingularAttribute<Course, Person> teacher;
	public static volatile SingularAttribute<Course, Date> lastUpdateDateTime;
	public static volatile SingularAttribute<Course, Long> lastUpdateBy;

}
