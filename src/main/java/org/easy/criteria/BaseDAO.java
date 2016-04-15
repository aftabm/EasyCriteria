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
package org.easy.criteria;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;

/**
 * Wrapper for EntityManger for CRUD operations and finders.
 * TBD: EntityListners
 */
public class BaseDAO
{
	private static Log log = LogFactory.getLog(BaseDAO.class);
	private CriteriaProcessor _criteriaProcessor = null;

	@PersistenceContext
	protected EntityManager _entityManager = null;

	public BaseDAO()
	{

	}

	public BaseDAO(EntityManager entityManager)
	{
		_entityManager = entityManager;
		_criteriaProcessor = new CriteriaProcessor(_entityManager);
	}

	/**
	 * @see {@link CriteriaProcessor#count(CriteriaComposer, boolean)} 
	 */
	public <T> long count(final CriteriaComposer<T> criteria, boolean distinct)
	{
		return _criteriaProcessor.count(criteria, distinct);
	}

	/**
	 * @see {@link javax.persistence.EntityManager#detach(Object)}
	 */
	public void detach(Object entity)
	{
		Preconditions.checkState(_entityManager != null);
		try
		{
			this._entityManager.detach(entity);
		}
		catch (RuntimeException re)
		{
			log.error("detach failed", re);
			throw new RuntimeException("detach failed");
		}
	}

	/**
	 * @see {@link org.easy.criteria.CriteriaProcessor#findAllEntity}
	 */
	public <E> List<E> findAll(Class<E> clazz)
	{
		log.debug("finding all instances of " + clazz);

		Preconditions.checkState(_entityManager != null);
		Preconditions.checkState(_criteriaProcessor != null);

		try
		{
			CriteriaComposer<E> findAllCriteria = CriteriaComposer.from(clazz);

			List<E> result = _criteriaProcessor.findAllEntity(findAllCriteria, false, null);
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("find failed", re);
			throw re;
		}
	}

	/**
	 * @see {@link org.easy.criteria.CriteriaProcessor#findAllEntity(CriteriaComposer, boolean, QueryProperties)}
	 */
	public <T> List<T> findAllEntity(CriteriaComposer<T> criteria,
	        boolean distinct, QueryProperties properties)
	{
		Preconditions.checkState(_criteriaProcessor != null);

		return _criteriaProcessor.findAllEntity(criteria, distinct, properties);

	}

	/**
	 * @see {@link org.easy.criteria.CriteriaProcessor#findAllTuple(CriteriaComposer, boolean, QueryProperties)}
	 */
	public <T> List<Tuple> findAllTuple(CriteriaComposer<T> criteria,
	        boolean distinct, QueryProperties properties)
	{
		Preconditions.checkState(_criteriaProcessor != null);
		return _criteriaProcessor.findAllTuple(criteria, distinct, properties);
	}

	/**
	 * @see {@link javax.persistence.EntityManager#find(Class, Object)}
	 */
	public <E> E findById(Class<E> clazz, Object id)
	{
		log.debug("finding instance of " + clazz + " with id: " + id);
		Preconditions.checkState(_entityManager != null);

		try
		{
			E instance = _entityManager.find(clazz, id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("find failed", re);
			throw re;
		}
	}

	/**
	 * @see {@link org.easy.criteria.CriteriaProcessor#findUniqueEntity(CriteriaComposer, QueryProperties)}
	 */
	public <T> T findUniqueEntity(CriteriaComposer<T> criteria,
			QueryProperties properties)
	{
		Preconditions.checkState(_criteriaProcessor != null);
		return _criteriaProcessor.findUniqueEntity(criteria, properties);
	}

	/**
	 * @see {@link org.easy.criteria.CriteriaProcessor#findUniqueTuple(CriteriaComposer, QueryProperties)}
	 */
	public <T> Tuple findUniqueTuple(CriteriaComposer<T> criteria,
			QueryProperties properties)
	{
		Preconditions.checkState(_criteriaProcessor != null);
		return _criteriaProcessor.findUniqueTuple(criteria, properties);
	}

	/**
	 * @see {@link javax.persistence.EntityManager#flush()}
	 */
	public void flushAndCommit()
	{
		Preconditions.checkState(_entityManager != null);

		try
		{
			FlushModeType flushModeType = _entityManager.getFlushMode();
			_entityManager.setFlushMode(FlushModeType.COMMIT);
			_entityManager.flush();
			_entityManager.setFlushMode(flushModeType);
		}
		catch (RuntimeException re)
		{
			log.error("flushAndCommit failed", re);
			throw new RuntimeException("flushAndCommit failed");
		}
	}

	/**
	 * @param timeZone
	 *            - default is UTC
	 * @return
	 */
	public Date getCurrentDateTime(String timeZone)
	{
		if (timeZone == null || timeZone.trim().length() == 0)
			timeZone = "UTC";

		return Calendar.getInstance(TimeZone.getTimeZone(timeZone)).getTime();
	}

	/**
	 * @return
	 */
	public EntityManager getEntityManager()
	{
		return _entityManager;
	}

	/**
	 * @see {@link javax.persistence.EntityManager#merge(Object)}
	 */
	public <E> E merge(E entity)
	{
		log.debug("updating instance of " + entity.getClass());
		Preconditions.checkState(_entityManager != null);

		try
		{
			E result = _entityManager.merge(entity);
			log.debug("update successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("update failed", re);
			throw new RuntimeException("Update failed");
		}
	}

	/**
	 * {@link javax.persistence.EntityManager#persist(Object))}
	 */
	public void persist(Object entity)
	{
		log.debug("saving instance of " + entity.getClass());

		Preconditions.checkState(_entityManager != null);

		try
		{
			_entityManager.persist(entity);
			log.debug("save successful");
		}
		catch (RuntimeException re)
		{
			log.error("save failed", re);
			throw new RuntimeException("Save failed");
		}
	}

	/**
	 * @see {@link javax.persistence.EntityManager#refresh(Object)}
	 */
	public void refresh(Object entity)
	{
		Preconditions.checkState(_entityManager != null);
		try
		{
			_entityManager.refresh(entity);
		}
		catch (RuntimeException re)
		{
			log.error("refresh failed", re);
			throw new RuntimeException("Refresh failed");
		}
	}

	/**
	 * 
	 * {@link javax.persistence.EntityManager#remove(Object))
	 */
	public <E> void remove(Class<E> clazz, Object id)
	{
		log.debug("deleting instance of " + clazz + " with id " + id);

		Preconditions.checkState(_entityManager != null);

		try
		{
			E entity = _entityManager.getReference(clazz, id);
			_entityManager.remove(entity);
			log.debug("delete successful");
		}
		catch (RuntimeException re)
		{
			log.error("delete failed", re);
			throw new RuntimeException("Delete failed");
		}
	}

	/**
	 * @param entityManager
	 */
	public void setEntityManager(EntityManager entityManager)
	{
		this._entityManager = entityManager;
	}
	
	
	/**
	 * Find all entities of the given type that are associated with this parent.
	 * @param childClass
	 * @param parent
	 * @return
	 */
	public <A, E> List<A> findByAssociation(Class<A> forClass, SingularAttribute<A, E> associationAttribute, E associatedWith)
	{
		
		if(log.isTraceEnabled())
			log.trace("findAssociatedEntity: ");

		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<A> criteria = criteriaBuilder.createQuery(forClass);
		
		log.debug("Root :"+forClass.getSimpleName());
		Root<A> child = criteria.from(forClass);
		
		criteria.distinct(true);
		
		criteria.where(criteriaBuilder.equal(child.get(associationAttribute), associatedWith));
		
		TypedQuery<A> query = _entityManager.createQuery(criteria);
		
		return query.getResultList();
	}
	
	
	/**
	 * @param forClass
	 * @param attributeToMatch
	 * @param value
	 * @return
	 */
	public <E, V> List<E> findByProperty(Class<E> forClass, SingularAttribute<E,V> attributeToMatch, V value)
	{
		
		if(log.isTraceEnabled())
			log.trace("findByProperty: ");
		
		Preconditions.checkNotNull(forClass);
		
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteria = criteriaBuilder.createQuery(forClass);
		
		log.debug("Root :"+forClass.getSimpleName());
		Root<E> child = criteria.from(forClass);
		
		criteria.distinct(true);
		
		Predicate predicate = criteriaBuilder.equal(child.get(attributeToMatch), value);
		criteria.where(predicate);
		
		TypedQuery<E> query = _entityManager.createQuery(criteria);
		return  query.getResultList();
	}

	
	
	
	/**
	 * {@link javax.persistence.EntityManager#getTransaction()#begin())}
	 */
	public EntityTransaction beginTransaction()
	{
		 EntityTransaction transaction = _entityManager.getTransaction();
		 transaction.begin();
		 return transaction;
	}
	
	/**
	 * {@link javax.persistence.EntityTransaction#isActive())}
	 *  {@link javax.persistence.EntityTransaction#commit())}
	 */
	public void commitTransaction(EntityTransaction transaction)
	{
		if (transaction.isActive())
			transaction.commit();
	}
	
	/**
	 * {@link javax.persistence.EntityTransaction#isActive())}
	 * {@link javax.persistence.EntityTransaction#rollback())}
	 */
	public void rollbackTransaction(EntityTransaction transaction)
	{
		if (transaction.isActive())
			transaction.rollback();
	}	
	


	public <T> Root<T> extractRoot(CriteriaComposer<T> criteria)
	{
		Preconditions.checkNotNull(criteria);
		
		Class<T> forClass = criteria.getEntityClass();
		
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
			
		Root<T> root = criteriaQuery.from(forClass);

		return root;
	}
	
	
	public <T,A> Path<A> extractPath(CriteriaComposer<T> criteria,
            SingularAttribute<T, A> attribute)
    {
		Preconditions.checkNotNull(criteria);
		Preconditions.checkNotNull(attribute);
		
		Class<T> forClass = criteria.getEntityClass();
		
		CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
			
		Root<T> root = criteriaQuery.from(forClass);
		
	    return root.get(attribute);
    }	
	
	
	
}
