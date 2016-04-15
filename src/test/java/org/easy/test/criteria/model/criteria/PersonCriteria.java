/**
 *   Copyright © 2011 Aftab Mahmood
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.

 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details <http://www.gnu.org/licenses/>.
 **/
package org.easy.test.criteria.model.criteria;

import static org.easy.criteria.CriteriaComposer.ComparisonOperator.EQUAL;

import javax.persistence.criteria.JoinType;

import org.easy.criteria.CriteriaComposer;
import org.easy.test.criteria.model.generated.Course;
import org.easy.test.criteria.model.generated.CourseSession;
import org.easy.test.criteria.model.generated.CourseSession_;
import org.easy.test.criteria.model.generated.Course_;
import org.easy.test.criteria.model.generated.Person;
import org.easy.test.criteria.model.generated.Person_;

public class PersonCriteria
{

	public static CriteriaComposer<Person> findStudent()
	{
		CriteriaComposer<Person> criteria = CriteriaComposer.from(Person.class);
		criteria.join(JoinType.INNER, Person_.personType,  PersonTypeCriteria.forStudentType());
		return criteria;

	}

	public static CriteriaComposer<Person> findStudentEnrolledInCourse(Course course)
	{
		CriteriaComposer<Person> criteria = PersonCriteria.findStudent();
		criteria.join(JoinType.INNER, Person_.courseSessions).
			where(CourseSession_.course, EQUAL, course);

		return criteria;

	}

	public static CriteriaComposer<Person> findStudentEnrolledInCourse(String courseName)
	{
		CriteriaComposer<Person> criteria = PersonCriteria.findStudent();
		criteria.join(JoinType.INNER, Person_.courseSessions).
			join(JoinType.INNER, CourseSession_.course).
			where(Course_.name, EQUAL, courseName);

		return criteria;

	}

	public static CriteriaComposer<Person> findTeacher()
	{
		CriteriaComposer<Person> criteria = CriteriaComposer.from(Person.class);
		criteria.join(JoinType.INNER, Person_.personType, PersonTypeCriteria.forTeacherType());
		return criteria;

	}

}
