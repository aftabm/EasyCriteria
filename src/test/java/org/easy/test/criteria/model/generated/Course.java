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
 * The persistent class for the course database table.
 * 
 */
@Entity
@Table(name = "course")
public class Course implements Serializable, Auditable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	// @Column(unique=true, nullable=false) derby bug# 789
	private Long id;

	@Column(length = 32)
	private String name;

	@Column()
	private Integer unit;
	
	public Integer getUnit()
    {
    	return unit;
    }

	public void setUnit(Integer unit)
    {
    	this.unit = unit;
    }

	// bi-directional many-to-one association to Person
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Person teacher;

	// bi-directional many-to-one association to CourseSession
	@OneToMany(mappedBy = "course")
	private Set<CourseSession> courseSessions;

	@Column(name = "last_update_date_time", nullable = false)
	private Date lastUpdateDateTime;

	@Column(name = "last_update_by", nullable = false)
	private Long lastUpdateBy;

	public Course()
	{
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

	public Person getTeacher()
	{
		return this.teacher;
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

	public void setTeacher(Person teacher)
	{
		this.teacher = teacher;
	}

}