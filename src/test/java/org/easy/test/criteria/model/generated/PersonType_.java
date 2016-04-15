package org.easy.test.criteria.model.generated;

import java.util.Date;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import org.easy.test.criteria.model.PersonTypeEnum;


@StaticMetamodel(PersonType.class)
public abstract class PersonType_
{
	public static volatile SingularAttribute<PersonType, Long> id;
	public static volatile SingularAttribute<PersonType, PersonTypeEnum> name;
	public static volatile SingularAttribute<PersonType, String> label;
	public static volatile SetAttribute<PersonType, Person> persons;
	public static volatile SingularAttribute<PersonType, Date> lastUpdateDateTime;
	public static volatile SingularAttribute<PersonType, Long> lastUpdateBy;

}
