/**
 * Copyright © 2011 Aftab Mahmood
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
package org.easy.criteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;

/**
 * 
 * Finds the data for the given criteria. Extracts the criteria information such
 * as Select clause, Where, Order By, Grouping, Having and attributes to match
 * (restrictions) from CriteriaContainer. Executes the criteria and generates
 * the result.
 * 
 * <pre>
 *  Example: 
 *  
 *  SQL : select person.name, sum(course.unit) from person inner join course_session inner join course group by person.name
 *	
 *	Using this framework: 
 *	    CriteriaComposer<Person> forStudentUnitCount = CriteriaComposer.createComposer(Person.class).select(Person_.name).groupBy(Person_.name);
 *	    forStudentUnitCount.join(Person_.courseSessions).join(CourseSession_.course).select(AggregateFunction.SUM, Course_.unit);
 *      List<Tuple>  result = criteriaProcessor.findAllTuple(forStudentUnitCount);
 * </pre>
 * @author mahmood.aftab
 */
public final class CriteriaProcessor
{
	private static Log log = LogFactory.getLog(CriteriaProcessor.class);

	@PersistenceContext
	protected EntityManager entityManager;

	public CriteriaProcessor(EntityManager entityManager)
	{
		log.trace("CriteriaProcessor");

		Preconditions.checkNotNull(entityManager, "EntityManager is null");
		this.entityManager = entityManager;
	}

	/**
	 * Counts the result found for the given criteria
	 * 
	 * @param criteria
	 * @param distinct
	 * @return
	 */
	public <T> long count(final CriteriaComposer<T> criteria, boolean distinct)
	{
		log.trace("CriteriaProcessor.count");

		Preconditions.checkNotNull(criteria);

		Class<T> forClass = criteria.getEntityClass();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder
		        .createQuery(Long.class);

		Root<T> root = criteriaQuery.from(forClass);

		log.debug("root =" + forClass.getName());

		if (distinct)
			criteriaQuery.select(criteriaBuilder.countDistinct(root));
		else
			criteriaQuery.select(criteriaBuilder.count(root));

		List<Predicate> predicates = new ArrayList<Predicate>();

		if (criteria != null)
		{
			criteria.generateJoins(root);
			criteria.generateWhere(criteriaBuilder, predicates);
		}

		criteriaQuery
		        .where(predicates.toArray(new Predicate[predicates.size()]));

		TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

		long result = query.getSingleResult();

		log.debug("CriteriaProcessor.count =" + result);

		return result;
	}
	
	
	
	/**
	 * @param <T>
	 * @param criteria
	 * @return
	 */
	public <T> List<T> findAllEntity(CriteriaComposer<T> criteria)
	{
		return this.findAllEntity(criteria, true, null);
			
	}
	

	/**
	 * Finds all entities that satisfied the given criteria. This ignores the
	 * "Select" clause. If you need to selected some specific columns then use
	 * "findAllTuple" API.
	 * 
	 * @param criteria
	 *            - A restriction criteria or NULL to get every thing.
	 * @param distinct
	 * @param startIndex
	 *            - Pass 0 or less to disable paging.
	 * @param maxResult
	 *            - Pass 0 or less to disable paging.
	 * @param lockMode
	 *            - Pass NULL if your are not managing transaction.
	 *            LockModeType.NONE will through exception if no transaction is
	 *            active.
	 * @return - A list of entities or an empty list if no result were found.
	 */
	public <T> List<T> findAllEntity(CriteriaComposer<T> criteria,
	        boolean distinct, QueryProperties properties)
	{
		log.trace("CriteriaProcessor.findAllEntity");

		Preconditions.checkNotNull(criteria);

		Class<T> forClass = criteria.getEntityClass();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(forClass);

		log.debug("root =" + forClass.getName());

		Root<T> root = criteriaQuery.from(forClass);

		criteriaQuery.distinct(distinct);

		List<Predicate> wherePredicates = new ArrayList<Predicate>();

		Map<Integer, Order> orderBy = new HashMap<Integer, Order>(0);

		if (criteria != null)
		{
			criteria.generateJoins(root);
			criteria.generateWhere(criteriaBuilder, wherePredicates);
			criteria.generateOrderBy(criteriaBuilder, orderBy);
		}

		criteriaQuery
		        .where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
		
		// Order by
		if (orderBy != null && orderBy.size() > 0)
		{
			Order[] orderByList = new Order[orderBy.size()];
			orderBy.values().toArray(orderByList);
			criteriaQuery.orderBy(orderByList);
		}

		TypedQuery<T> query = entityManager.createQuery(criteriaQuery);

		if (properties!=null)
			properties.applyProperties(query);

		List<T> result = query.getResultList();

		if (result == null)
			result = new ArrayList<T>(0);

		log.debug("CriteriaProcessor.findAllEntity result size=" + result.size());

		return result;
	}
	
	
	
	/**
	 * @param <T>
	 * @param criteria
	 * @return
	 */
	public <T> List<Tuple> findAllTuple(CriteriaComposer<T> criteria)
	{
		return this.findAllTuple(criteria,  true, null);
	}

	/**
	 * Finds all the tuples for the given criteria. Make sure you have provided
	 * columns information in CriteriaContainer that you want in this tuple
	 * result-set.
	 * 
	 * @param criteria
	 *            - Criteria that you want to apply to this search.
	 * @param distinct
	 *            -
	 * @param startIndex
	 *            - Pass 0 or less to disable paging.
	 * @param maxResult
	 *            - Pass 0 or less to disable paging.
	 * @param lockMode
	 *            - Pass NULL if your are not managing transaction.
	 *            LockModeType.NONE will through exception if no transaction is
	 *            active.
	 * @return - A list of tuples or an empty list if no result were found.
	 */
	public <T> List<Tuple> findAllTuple(CriteriaComposer<T> criteria,
	        boolean distinct, QueryProperties properties)
	{

		log.trace("CriteriaProcessor.findAllTuple");

		Preconditions.checkNotNull(criteria);

		Class<T> forClass = criteria.getEntityClass();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder
		        .createQuery(Tuple.class);

		log.debug("root =" + forClass.getName());
		Root<T> root = criteriaQuery.from(forClass);

		List<Predicate> wherePredicates = new ArrayList<Predicate>();
		List<Selection<?>> selectPredicates = new ArrayList<Selection<?>>();
		Map<Integer, Order> orderByPredicates = new HashMap<Integer, Order>(0);
		Map<Integer, Expression<?>> groupByPredicates = new HashMap<Integer, Expression<?>>(0);
		List<Predicate> havingPredicates = new ArrayList<Predicate>(0);

		if (criteria != null)
		{
			criteria.generateJoins(root);
			criteria.generateSelect(criteriaBuilder, selectPredicates);
			criteria.generateGroupBy(criteriaBuilder, groupByPredicates);
			criteria.generateWhere(criteriaBuilder, wherePredicates);
			criteria.generateOrderBy(criteriaBuilder, orderByPredicates);
			criteria.generateHaving(criteriaBuilder, havingPredicates);
		}

		Preconditions.checkState(selectPredicates!=null);
		Preconditions.checkArgument(selectPredicates.size()>0, "No column name found for select clause. " +
				"Atleast one should be provided for Tuple result. Consider using findAllEntity instead.");
		
		criteriaQuery.multiselect(selectPredicates);
		
		criteriaQuery
		        .where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

		if (orderByPredicates != null && orderByPredicates.size() > 0)
		{
			Order[] orderByList = new Order[orderByPredicates.size()];
			orderByPredicates.values().toArray(orderByList);
			criteriaQuery.orderBy(orderByList);
		}
		
		if (groupByPredicates!=null && groupByPredicates.size()>0)
		{
			Expression<?>[] groupByList = new Expression<?>[groupByPredicates.size()];
			groupByPredicates.values().toArray(groupByList);
			criteriaQuery.groupBy(groupByList);
		}
		
		criteriaQuery.having(havingPredicates.toArray(new Predicate[havingPredicates.size()]));

		TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);

		if (properties!=null)
			properties.applyProperties(query);

		List<Tuple> tuples = query.getResultList();

		if (tuples == null)
			tuples = new ArrayList<Tuple>(0);

		log.debug("CriteriaProcessor.findAllEntity result size=" + tuples.size());

		return tuples;
	}

	/**
	 * Finds a single entity for the given criteria. This ignores the "Select"
	 * clause of CriteriaContainer. If you need to selected some specific
	 * columns
	 * then use "findUniqueTuple" API.
	 * 
	 * @param criteria
	 * @param lockMode
	 *            - Pass NULL if your are not managing transaction.
	 *            LockModeType.NONE will through exception if no transaction is
	 *            active.
	 * @return - A single entity or null if no result was found.
	 */
	public <T> T findUniqueEntity(CriteriaComposer<T> criteria,
			QueryProperties properties)
	{
		log.trace("CriteriaProcessor.findUniqueEntity");
		Preconditions.checkNotNull(criteria);

		Class<T> forClass = criteria.getEntityClass();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(forClass);

		log.debug("root =" + forClass.getName());
		Root<T> root = criteriaQuery.from(forClass);

		List<Predicate> wherePredicates = new ArrayList<Predicate>();

		if (criteria != null)
		{
			criteria.generateJoins(root);
			criteria.generateWhere(criteriaBuilder, wherePredicates);
		}

		criteriaQuery
		        .where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

		TypedQuery<T> query = entityManager.createQuery(criteriaQuery);

		if (properties!=null)
			properties.applyProperties(query);

		return query.getSingleResult();
	}
	
	/**
	 * @param <T>
	 * @param criteria
	 * @return
	 */
	public <T> Tuple findUniqueTuple(CriteriaComposer<T> criteria )	
	{
		return this.findUniqueTuple(criteria,null);
	}

	/**
	 * Finds single tuple for the given criteria. Make sure you have provided
	 * columns information in CriteriaContainer that you want in this tuple
	 * result-set.
	 * 
	 * @param criteria
	 * @param lockMode
	 * @return - A single tuple or null if no result was found.
	 */
	public <T> Tuple findUniqueTuple(CriteriaComposer<T> criteria,
			QueryProperties properties)
	{
		log.trace("CriteriaProcessor.findUniqueTuple");
		Preconditions.checkNotNull(criteria);

		Class<T> forClass = criteria.getEntityClass();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder
		        .createQuery(Tuple.class);

		log.debug("root =" + forClass.getName());
		Root<T> root = criteriaQuery.from(forClass);

		List<Predicate> wherePredicates = new ArrayList<Predicate>();
		List<Selection<?>> selectAttributes = new ArrayList<Selection<?>>();

		if (criteria != null)
		{
			criteria.generateJoins(root);
			criteria.generateSelect(criteriaBuilder, selectAttributes);
			criteria.generateWhere(criteriaBuilder, wherePredicates);
		}

		Preconditions.checkState(selectAttributes!=null);
		Preconditions.checkArgument(selectAttributes.size()>0, "No column name found for select clause. " +
				"Atleast one should be provided for Tuple result. Consider using findUniqueEntity instead.");
		
		criteriaQuery.multiselect(selectAttributes);
		
		criteriaQuery
		        .where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

		TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);

		if (properties!=null)
			properties.applyProperties(query);

		return query.getSingleResult();
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}


	/**
	 * @param query
	 * @param startIndex
	 * @param maxResult
	 */
	private void setPagination(Query query, int startIndex, int maxResult)
	{
		// Pagination
		if (startIndex >= 0)
		{
			log.debug("startIndex = " + startIndex);
			query.setFirstResult(startIndex*maxResult);
		}

		if (maxResult > 0)
		{
			log.debug("maxResult = " + maxResult);
			query.setMaxResults(maxResult);
		}

	}

}
