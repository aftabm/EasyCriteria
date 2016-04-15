package org.easy.test.criteria.model.generated;

import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CourseSession.class)
public abstract class CourseSession_
{

	public static volatile SingularAttribute<CourseSession, Course> course;
	public static volatile SingularAttribute<CourseSession, Long> id;
	public static volatile SingularAttribute<CourseSession, Person> student;
	public static volatile SingularAttribute<CourseSession, Date> lastUpdateDateTime;
	public static volatile SingularAttribute<CourseSession, Long> lastUpdateBy;

}
