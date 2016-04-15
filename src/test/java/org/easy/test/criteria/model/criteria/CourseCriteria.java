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

import java.util.Date;

import org.easy.criteria.CriteriaComposer;
import org.easy.criteria.CriteriaComposer.ComparisonOperator;
import org.easy.test.criteria.model.generated.Course;
import org.easy.test.criteria.model.generated.Course_;

/**
 * Maintains all the criteria related to Course entity
 * 
 * @author mahmood.aftab
 * 
 */
public class CourseCriteria
{

	public static CriteriaComposer<Course> findCouorseUpdatedBetween(Date thatDate, Date toDate)
	{
		CriteriaComposer<Course> criteria = CriteriaComposer.from(Course.class);
		criteria.where(Course_.lastUpdateDateTime, ComparisonOperator.BETWEEN, thatDate, toDate);

		return criteria;
	}

	public static CriteriaComposer<Course> findCourseByName(String courseName)
	{
		CriteriaComposer<Course> criteria = CriteriaComposer.from(Course.class);
		criteria.where(Course_.name, ComparisonOperator.EQUAL, courseName);

		return criteria;
	}

}
