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

import org.easy.criteria.CriteriaComposer;
import org.easy.criteria.CriteriaComposer.ComparisonOperator;
import org.easy.test.criteria.model.PersonTypeEnum;
import org.easy.test.criteria.model.generated.PersonType;
import org.easy.test.criteria.model.generated.PersonType_;

/**
 * Maintains all the criteria related to PersonTypeCriteria entity
 * 
 * @author mahmood.aftab
 * 
 */
public class PersonTypeCriteria
{
	public static CriteriaComposer<PersonType> forAdminType()
	{
		CriteriaComposer<PersonType> criteria = CriteriaComposer.from(PersonType.class);
		criteria.where(PersonType_.name, ComparisonOperator.EQUAL, PersonTypeEnum.ADMIN);

		return criteria;
	}

	public static CriteriaComposer<PersonType> forStudentType()
	{
		CriteriaComposer<PersonType> criteria = CriteriaComposer.from(PersonType.class);
		criteria.where(PersonType_.name, ComparisonOperator.EQUAL, PersonTypeEnum.STUDENT);

		return criteria;
	}

	public static CriteriaComposer<PersonType> forTeacherType()
	{
		CriteriaComposer<PersonType> criteria = CriteriaComposer.from(PersonType.class);
		criteria.where(PersonType_.name, ComparisonOperator.EQUAL, PersonTypeEnum.TEACHER);

		return criteria;
	}
}
