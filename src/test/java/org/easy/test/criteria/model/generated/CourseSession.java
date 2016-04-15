package org.easy.test.criteria.model.generated;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.easy.test.criteria.model.Auditable;


/**
 * The persistent class for the course_session database table.
 * 
 */
@Entity
@Table(name = "course_session")
public class CourseSession implements Serializable, Auditable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Long id;

	// bi-directional many-to-one association to Person
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Person student;

	// bi-directional many-to-one association to Course
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(name = "last_update_date_time", nullable = false)
	private Date lastUpdateDateTime;

	@Column(name = "last_update_by", nullable = false)
	private Long lastUpdateBy;

	public CourseSession()
	{
	}

	public Course getCourse()
	{
		return this.course;
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

	public Person getStudent()
	{
		return this.student;
	}

	public void setCourse(Course course)
	{
		this.course = course;
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

	public void setStudent(Person student)
	{
		this.student = student;
	}

}