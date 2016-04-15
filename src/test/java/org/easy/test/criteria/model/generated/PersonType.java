package org.easy.test.criteria.model.generated;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.easy.test.criteria.model.Auditable;
import org.easy.test.criteria.model.PersonTypeEnum;


/**
 * The persistent class for the person_type database table.
 * 
 */
@Entity
@Table(name = "person_type")
public class PersonType implements Serializable, Auditable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Long id;

	@Column(length = 32)
	private String label;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private PersonTypeEnum name;

	// bi-directional many-to-one association to Person
	@OneToMany(mappedBy = "personType")
	private Set<Person> persons;

	@Column(name = "last_update_date_time", nullable = false)
	private Date lastUpdateDateTime;

	@Column(name = "last_update_by", nullable = false)
	private Long lastUpdateBy;

	public PersonType()
	{
	}

	public Long getId()
	{
		return this.id;
	}

	public String getLabel()
	{
		return this.label;
	}

	public Long getLastUpdateBy()
	{
		return lastUpdateBy;
	}

	public Date getLastUpdateDateTime()
	{
		return lastUpdateDateTime;
	}

	public PersonTypeEnum getName()
	{
		return this.name;
	}

	public Set<Person> getPersons()
	{
		return this.persons;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setLastUpdateBy(Long lastUpdateBy)
	{
		this.lastUpdateBy = lastUpdateBy;
	}

	public void setLastUpdateDateTime(Date lastUpdateDateTime)
	{
		this.lastUpdateDateTime = lastUpdateDateTime;
	}

	public void setName(PersonTypeEnum name)
	{
		this.name = name;
	}

	public void setPersons(Set<Person> persons)
	{
		this.persons = persons;
	}

}