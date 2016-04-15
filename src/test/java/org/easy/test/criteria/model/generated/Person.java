package org.easy.test.criteria.model.generated;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.easy.test.criteria.model.Auditable;


/**
 * The persistent class for the person database table.
 * 
 */
@Entity
@Table(name = "person")
public class Person implements Serializable, Auditable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Long id;

	@Column(length = 32)
	private String name;

	// bi-directional many-to-one association to Course
	@OneToMany(mappedBy = "teacher")
	private Set<Course> courses;

	// bi-directional many-to-one association to CourseSession
	@OneToMany(mappedBy = "student")
	private Set<CourseSession> courseSessions;

	// bi-directional many-to-one association to PersonType
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_type_id", nullable = false)
	private PersonType personType;

	@Column(name = "last_update_date_time", nullable = false)
	private Date lastUpdateDateTime;

	@Column(name = "last_update_by", nullable = false)
	private Long lastUpdateBy;

	public Person()
	{
	}

	public Set<Course> getCourses()
	{
		return this.courses;
	}

	public Set<CourseSession> getCourseSessions()
	{
		return this.courseSessions;
	}

	public Long getId()
	{
		return this.id;
	}

	public Long getLastUpdateBy()
	{
		return lastUpdateBy;
	}

	public Date getLastUpdateDateTime()
	{
		return lastUpdateDateTime;
	}

	public String getName()
	{
		return this.name;
	}

	public PersonType getPersonType()
	{
		return this.personType;
	}

	public void setCourses(Set<Course> courses)
	{
		this.courses = courses;
	}

	public void setCourseSessions(Set<CourseSession> courseSessions)
	{
		this.courseSessions = courseSessions;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setLastUpdateBy(Long lastUpdateBy)
	{
		this.lastUpdateBy = lastUpdateBy;
	}

	public void setLastUpdateDateTime(Date lastUpdateDateTime)
	{
		this.lastUpdateDateTime = lastUpdateDateTime;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPersonType(PersonType personType)
	{
		this.personType = personType;
	}
}