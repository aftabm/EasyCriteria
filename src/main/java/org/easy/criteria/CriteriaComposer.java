/**
 * Copyright © 2011 Aftab Mahmood
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details <http://www.gnu.org/licenses/>.
 **/
package org.easy.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.base.Preconditions;

/**
 * Encapsulates all the information that may required to search entity(ies) or
 * tuple(s). This includes columns to select for Tuple, attributes to match in
 * where clause, _joinContainer order by, having, and group by attributes. All
 * this information is provided in form of metadata which makes this criteria
 * composer type safe.
 * 
 * <pre>
 * Example 1: 
 *  
 *  SQL : select person.name, sum(course.unit) from person inner join course_session inner join course group by person.name
 * 
 * Using this framework: 
 *     CriteriaComposer<Person> forStudentUnitCount = CriteriaComposer.createComposer(Person.class).select(Person_.name).groupBy(Person_.name);
 *     forStudentUnitCount.join(Person_.courseSessions).join(CourseSession_.course).select(AggregateFunction.SUM, Course_.unit);
 *      List<Tuple>  result = criteriaProcessor.findAllTuple(forStudentUnitCount);
 *      
 * Example 2:
 * 
 * 	SQL: select * from course where course.last_update_date_time between ? and ?
 * 
 * 		CriteriaComposer<Course> courseCriteria = new CriteriaComposer<Course>(Course.class);
 * 	    criteria.where(Course_.lastUpdateDateTime, ComparisonOperator.BETWEEN, thatDate, toDate);
 * 
 * 	List<Course> result = criteriaProcessor.findAllEntity(courseCriteria);
 * </pre>
 * 
 * @author mahmood.aftab
 * 
 * @param <E>
 *            - The root entity for this criteria.
 */
public class CriteriaComposer<E>
{
	public enum AggregateFunction {
		COUNT, AVG, SUM, MIN, MAX
	}

	public enum ComparisonOperator {
		EQUAL, LIKE, IN, BETWEEN, IS_NULL, LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL
	};
	
	private enum RangOperator{IN, BETWEEN}; //TBD remove if not needed
	
	private enum LogicOperator {
		AND, OR, NONE
	}

	public enum NegationOperator {
		NOT
	}

	
	private enum LastCallType{WHERE, HAVING};
	
	private LastCallType lastCallType=null;
	
	private Class<E> _entityClass;
	
	
	@SuppressWarnings("rawtypes")
	private From root;
	private List<WhereContainer<E>> _wheres = new ArrayList<WhereContainer<E>>( 0);
	private Map<JoinContainer<E>, CriteriaComposer<?>> _joins = new LinkedHashMap<JoinContainer<E>, CriteriaComposer<?>>(0);
	private List<SelectContainer<E>> _selects = new ArrayList<SelectContainer<E>>(0);;
	private List<OrderByContainer<E>> _orderBys = new ArrayList<OrderByContainer<E>>(0);
	private List<GroupByContainer<E>> _groupBys = new ArrayList<GroupByContainer<E>>(0);
	private List<HavingContainer<E>> _havings = new ArrayList<HavingContainer<E>>(0);
	private List<Selection<?>> multiselect=new ArrayList<Selection<?>>(0);
	

	public void merge(CriteriaComposer<E> other)
	{
		this._joins.putAll(other._joins);
		this._selects.addAll(other._selects);
		this._orderBys.addAll(other._orderBys);
		this._groupBys.addAll(other._groupBys);
		this._havings.addAll(other._havings);
		this._wheres.addAll(other._wheres);
	}
	
	protected <V> Predicate getPredicate(SingularAttribute<E, V> attribute)
	{
		Preconditions.checkState(root!=null,"root is null. Either join is not defined or not generated.");
		//TBD
		
		return null;
	}
	
	
	
	
	/**
	 * Calls CriteriaBuilder's API for the respective operator. Add new case if
	 * you add a new operator.
	 * 
	 * @author mahmood.aftab
	 * 
	 */
	private static class ComparisonOperatorProcessor
	{
		protected static Predicate negate(
		        final CriteriaBuilder criteriaBuilder, final Predicate predicate)
		{
			return criteriaBuilder.not(predicate);
		}
		
		
		@SuppressWarnings("rawtypes")
        private static Predicate processSubquery(
		        final CriteriaBuilder criteriaBuilder, final Expression attribute,
		        final ComparisonOperator cOperator, final CriteriaComposer<?> value)
		{
			return null;
		}
		

		@SuppressWarnings({ "unchecked", "rawtypes" })
        private static Predicate processParameter(
		        final CriteriaBuilder criteriaBuilder, final Expression attribute,
		        final ComparisonOperator cOperator, final Object value)
		{
			Predicate out;
			List<?> values;

			switch (cOperator)
			{
			case EQUAL:
				out = criteriaBuilder.equal(attribute, value);
				break;

			case IN:
				Preconditions.checkState(value instanceof java.util.List,
				        "Value for IN opeations should be of type List");
				values = (List<?>) value;
				Preconditions.checkArgument(values.size() > 0);

				out = attribute.in(values.toArray());
				break;

			case LIKE:
				out = criteriaBuilder.like(attribute, value.toString());
				break;

			case BETWEEN:
				Preconditions.checkState(value instanceof java.util.List,
				        "Values for between opeations should be of type Array");
				values = (List<?>) value;
				Preconditions.checkArgument(values.size() == 2, "BETWEEN operation cannot be applied to more than two values.");
				Preconditions
				        .checkState(
				                values.get(0) instanceof java.lang.Comparable,
				                "Values for between opeations should implement Compareable");

				out = criteriaBuilder.between(attribute,
				        (java.lang.Comparable) values.get(0),
				        (java.lang.Comparable) values.get(1));
				break;

			case IS_NULL:
				out = criteriaBuilder.isNull(attribute);
				break;

			case GREATER_THAN:
				Preconditions
				        .checkState(value instanceof java.lang.Comparable,
				                "Value for GREATER_THAN opeations should implement Compareable");
				out = criteriaBuilder.greaterThan(attribute,
				        (java.lang.Comparable) value);
				break;

			case GREATER_THAN_EQUAL:
				Preconditions
				        .checkState(value instanceof java.lang.Comparable,
				                "Value for GREATER_THAN opeations should implement Compareable");
				out = criteriaBuilder.greaterThanOrEqualTo(attribute,
				        (java.lang.Comparable) value);
				break;

			case LESS_THAN:
				Preconditions
				        .checkState(value instanceof java.lang.Comparable,
				                "Value for GREATER_THAN opeations should implement Compareable");
				out = criteriaBuilder.lessThan(attribute,
				        (java.lang.Comparable) value);
				break;

			case LESS_THAN_EQUAL:
				Preconditions
				        .checkState(value instanceof java.lang.Comparable,
				                "Value for GREATER_THAN opeations should implement Compareable");
				out = criteriaBuilder.lessThanOrEqualTo(attribute,
				        (java.lang.Comparable) value);
				break;

			default:
				throw new java.lang.IllegalStateException(
				        "Unknown operator found " + cOperator.toString());
			}

			return out;
		}
	
		
		@SuppressWarnings({ "rawtypes" })
		protected static Predicate process(
		        final CriteriaBuilder criteriaBuilder, final Expression attribute,
		        final ComparisonOperator cOperator, final Object value)
		{
			if (value instanceof CriteriaComposer)
				return processSubquery(criteriaBuilder, attribute, cOperator, (CriteriaComposer<?>)value);
			else
				return processParameter(criteriaBuilder, attribute, cOperator, value);
		}
	}

	/**
	 * @author mahmood.aftab
	 * 
	 *         SELECT column_name, aggregate_function(column_name) FROM
	 *         table_name WHERE column_name operator value GROUP BY column_name
	 */
	private class GroupByContainer<T>
	{
		SingularAttribute<? super T, ?> attribute;
		Integer rank;

		public GroupByContainer(SingularAttribute<? super T, ?> attribute, int rank)
		{
			this.attribute = attribute;
			this.rank = rank;
		}

		@SuppressWarnings("unchecked")
        @Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GroupByContainer<T> other = (GroupByContainer<T>) obj;
			if (attribute == null)
			{
				if (other.attribute != null)
					return false;
			}
			else
				if (!attribute.equals(other.attribute))
					return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
			        + ((attribute == null) ? 0 : attribute.hashCode());
			result = prime * result + ((rank == null) ? 0 : rank.hashCode());
			return result;
		}

		@Override
		public String toString()
		{
			return "GroupByContainer [attribute=" + attribute + ", rank="
			        + rank + "]";
		}
	}

	/**
	 * Stores information for a single having clause.
	 * 
	 * @param <T>
	 */
	private class HavingContainer<T>
	{
		AggregateFunction function=null;
		ComparisonOperator comparisionOperator=ComparisonOperator.EQUAL;
		LogicOperator logicOperator=LogicOperator.NONE;
		NegationOperator notOperator=null;
		Object value=null;
		SingularAttribute<? super T, ?> attribute=null;

		<V> HavingContainer(AggregateFunction function,
		        SingularAttribute<? super T, V> attribute,
		        ComparisonOperator comparisionOperator, V value)
		{
			this(function, attribute, null, comparisionOperator, value);
		}


		<V> HavingContainer(AggregateFunction function,
		        SingularAttribute<? super T, V> attribute,
		        NegationOperator notOperator,
		        ComparisonOperator comparisionOperator, V value)
		{
			this.function = function;
			this.comparisionOperator = comparisionOperator;
			this.notOperator = notOperator;
			this.value = value;
			this.attribute = attribute;
		}

		@Override
        public String toString()
        {
	        return "HavingContainer [function=" + function + ", comparisionOperator=" + comparisionOperator
	                + ", logicOperator=" + logicOperator + ", notOperator=" + notOperator + ", value=" + value
	                + ", attribute=" + attribute + "]";
        }


		@Override
        public int hashCode()
        {
	        final int prime = 31;
	        int result = 1;
	        result = prime * result + getOuterType().hashCode();
	        result = prime * result + ((function == null) ? 0 : function.hashCode());
	        result = prime * result + ((value == null) ? 0 : value.hashCode());
	        return result;
        }


		@Override
        public boolean equals(Object obj)
        {
	        if (this == obj)
		        return true;
	        if (obj == null)
		        return false;
	        if (getClass() != obj.getClass())
		        return false;
	        HavingContainer other = (HavingContainer) obj;
	        if (!getOuterType().equals(other.getOuterType()))
		        return false;
	        if (function != other.function)
		        return false;
	        if (value == null)
	        {
		        if (other.value != null)
			        return false;
	        }
	        else
		        if (!value.equals(other.value))
			        return false;
	        return true;
        }
		

		private CriteriaComposer getOuterType()
        {
	        return CriteriaComposer.this;
        }
	}
	/**
	 * Internal class that contains attributes for Joins.
	 * 
	 * @author mahmood.aftab
	 * 
	 * @param <T>
	 */
	private class JoinContainer<T>
	{
		JoinType joinType;
		SingularAttribute<? super T, ?> singularAttribute;
		SetAttribute<? super T, ?> setAttribute;

		JoinContainer(JoinType joinType, SetAttribute<? super T, ?> setAttribute)
		{
			this.joinType = joinType;
			this.setAttribute = setAttribute;
		}

		JoinContainer(JoinType joinType,
		        SingularAttribute<? super T, ?> singularAttribute)
		{
			this.singularAttribute = singularAttribute;
			this.joinType = joinType;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JoinContainer<T> other = (JoinContainer) obj;
			if (setAttribute == null)
			{
				if (other.setAttribute != null)
					return false;
			}
			else
				if (!setAttribute.equals(other.setAttribute))
					return false;
			if (singularAttribute == null)
			{
				if (other.singularAttribute != null)
					return false;
			}
			else
				if (!singularAttribute.equals(other.singularAttribute))
					return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
			        + ((setAttribute == null) ? 0 : setAttribute.hashCode());
			result = prime
			        * result
			        + ((singularAttribute == null) ? 0 : singularAttribute
			                .hashCode());
			return result;
		}

		@Override
		public String toString()
		{
			return "JoinContainer [joinType=" + joinType
			        + ", singularAttribute=" + singularAttribute
			        + ", setAttribute=" + setAttribute + "]";
		}
	}

	/**
	 * @author mahmood.aftab
	 * 
	 */
	private class OrderByContainer<T>
	{
		SingularAttribute<? super T, ?> attribute;
		boolean ascending;
		Integer rank;

		OrderByContainer(SingularAttribute<? super T, ?> attribute,
		        boolean ascending, int rank)
		{
			this.attribute = attribute;
			this.ascending = ascending;
			this.rank = rank;
		}

		@SuppressWarnings("rawtypes")
        @Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OrderByContainer other = (OrderByContainer) obj;
			if (attribute == null)
			{
				if (other.attribute != null)
					return false;
			}
			else
				if (!attribute.equals(other.attribute))
					return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (ascending ? 1231 : 1237);
			result = prime * result
			        + ((attribute == null) ? 0 : attribute.hashCode());
			result = prime * result + ((rank == null) ? 0 : rank.hashCode());
			return result;
		}

		@Override
		public String toString()
		{
			return "OrderByContainer [attribute=" + attribute + ", ascending="
			        + ascending + ", rank=" + rank + "]";
		}
	}
	/**
	 * /** Internal class that contains attributes that Tuple should select as
	 * part of the result set.
	 * 
	 * @author mahmood.aftab
	 * 
	 * @param <T>
	 */

	private class SelectContainer<T>
	{
		SingularAttribute<? super T, ?> singularAttribute;
		String alias;

		AggregateFunction function;

		SelectContainer(AggregateFunction function,
		        SingularAttribute<? super T, ?> singularAttribute, String alias)
		{
			this.singularAttribute = singularAttribute;
			this.alias = alias;
			this.function = function;
		}

		SelectContainer(SingularAttribute<? super T, ?> singularAttribute, String alias)
		{
			this.singularAttribute = singularAttribute;
			this.alias = alias;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SelectContainer<T> other = (SelectContainer) obj;
			if (singularAttribute == null)
			{
				if (other.singularAttribute != null)
					return false;
			}
			else
				if (!singularAttribute.equals(other.singularAttribute))
					return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
			        + ((function == null) ? 0 : function.hashCode());
			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
			result = prime
			        * result
			        + ((singularAttribute == null) ? 0 : singularAttribute
			                .hashCode());
			return result;
		}

		@Override
		public String toString()
		{
			return "SelectContainer [singularAttribute=" + singularAttribute
			        + ", alias=" + alias + ", function=" + function + "]";
		}
	}
	/**
	 * Internal class that contains attributes to match for where clause.
	 * 
	 * @author mahmood.aftab
	 * 
	 * @param <T>
	 */
	public class WhereContainer<T>
	{

		ComparisonOperator comparisionOperator=ComparisonOperator.EQUAL;
		LogicOperator logicOperator=LogicOperator.NONE;
		NegationOperator notOperator=null;
		Object value=null;
		SingularAttribute<? super T, ?> attribute=null;
		boolean containsSubQuery = false;

		// SetAttribute<E, ?> setattribute;

		<V> WhereContainer(SingularAttribute<? super T, V> attribute,
		        ComparisonOperator cOperator, V value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.value = value;
		}

		<V> WhereContainer(SingularAttribute<? super T, V> attribute,
		        NegationOperator nOperator, ComparisonOperator cOperator,
		        V value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.notOperator = nOperator;
			this.value = value;
		}
		
		
		<V> WhereContainer(SingularAttribute<? super T, V> attribute,
		        ComparisonOperator cOperator, CriteriaComposer<V> value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.value = value;
			this.containsSubQuery = true;
		}

		<V> WhereContainer(SingularAttribute<? super T, ?> attribute,
		        NegationOperator nOperator, ComparisonOperator cOperator,
		        CriteriaComposer<V> value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.notOperator = nOperator;
			this.value = value;
			this.containsSubQuery = true;
		}
		
		<V> WhereContainer(SingularAttribute<? super T, V> attribute,
		        ComparisonOperator cOperator, List<V> value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.value = value;
		}

		<V> WhereContainer(SingularAttribute<? super T, V> attribute,
		        NegationOperator nOperator, ComparisonOperator cOperator,
		        List<V> value)
		{
			this.attribute = attribute;
			this.comparisionOperator = cOperator;
			this.notOperator = nOperator;
			this.value = value;
		}		
		

		@SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WhereContainer<T> other = (WhereContainer) obj;
			if (attribute == null)
			{
				if (other.attribute != null)
					return false;
			}
			else
				if (!attribute.equals(other.attribute))
					return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
			        + ((attribute == null) ? 0 : attribute.hashCode());
			result = prime
			        * result
			        + ((comparisionOperator == null) ? 0 : comparisionOperator
			                .hashCode());
			result = prime * result
			        + ((notOperator == null) ? 0 : notOperator.hashCode());
			result = prime * result
			        + ((logicOperator == null) ? 0 : logicOperator.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public String toString()
		{
			return "WhereContainer [attributeToValueOperator="
			        + comparisionOperator + ", nextWhereOperator="
			        + logicOperator + ", negationOperator=" + notOperator
			        + ", value=" + value + ", attribute=" + attribute + "]";
		}

	}
	private static Log log = LogFactory.getLog(CriteriaComposer.class);

	/**
	 * @param forClass
	 * @return
	 * @deprecated  As of release 2.0 beta, replaced by {@link #from(Class<E> forClass)}
	 */
	@Deprecated
	public static <E> CriteriaComposer<E> createComposer(Class<E> forClass)
	{
		return new CriteriaComposer<E>(forClass);
	}

	/**
	 * @param forClass
	 * @return
	 */
	public static <E> CriteriaComposer<E> from(Class<E> forClass)
	{
		return new CriteriaComposer<E>(forClass);
	}
	

	/**
	 * @param forClass
	 */
	private CriteriaComposer(Class<E> forClass)
	{
		this._entityClass = forClass;
	}

	
	protected void generateExampleBy()
	{
		
	}
	
	/**
	 * @param criteriaBuilder
	 * @param out
	 */
	@SuppressWarnings("unchecked")
    protected void generateGroupBy(final CriteriaBuilder criteriaBuilder,
	        Map<Integer, Expression<?>> out)
	{
		log.trace("generateOrderBy");

		Preconditions.checkNotNull(out);
		Preconditions.checkNotNull(root);
		Preconditions.checkNotNull(criteriaBuilder);

		// First others
		if (_joins != null)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> join : allSetJoins)
			{
				CriteriaComposer<?> subCriteria = join.getValue();

				if (subCriteria != null)
					subCriteria.generateGroupBy(criteriaBuilder, out);
			}
		}

		// Then me. I will overwrite rank of others.
		if (_groupBys != null && _groupBys.size() > 0)
		{
			for (GroupByContainer<E> groupByContainer : _groupBys)
			{
				out.put(groupByContainer.rank,
				        root.get(groupByContainer.attribute));
				log.debug("adding _groupBy "
				        + groupByContainer.attribute.getName());
			}
		}

	}

	/**
	 * @param criteriaBuilder
	 * @param out
	 */
	@SuppressWarnings("unchecked")
    protected void generateHaving(final CriteriaBuilder criteriaBuilder, final List<Predicate> out)
	{
		// log.debug("generateHaving :");

		Preconditions.checkNotNull(out);
		Preconditions.checkNotNull(criteriaBuilder);
		Preconditions.checkNotNull(root);

		if (_joins != null && _joins.size() > 0)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> joinEntry : allSetJoins)
			{
				CriteriaComposer<?> subCriteria = joinEntry.getValue();

				if (subCriteria != null)
					subCriteria.generateHaving(criteriaBuilder, out);
			}
		}

		if (_havings != null)
		{
			List<Predicate> andPredicates = new ArrayList<Predicate>(0);
			List<Predicate> orPredicates = new ArrayList<Predicate>(0);

			LogicOperator lastOperator = LogicOperator.NONE;

			for (HavingContainer<E> havingContainer : _havings)
			{
				
				@SuppressWarnings("rawtypes")
                Expression expression = null;

				switch (havingContainer.function)
				{
				case COUNT:
					expression = criteriaBuilder.count(root
					                    .get(havingContainer.attribute));
					break;

				case AVG:
					expression = criteriaBuilder.avg(root
					                    .get(havingContainer.attribute));
					break;

				case SUM:
					expression = criteriaBuilder.sum(root
					                    .get(havingContainer.attribute));
					break;

				case MAX:
					expression = criteriaBuilder.max(root
					                    .get(havingContainer.attribute));
					break;

				case MIN:
					expression = criteriaBuilder.min(root
					                    .get(havingContainer.attribute));
					break;
				}
				
				Predicate predicate = ComparisonOperatorProcessor.process(
				        criteriaBuilder, expression,
				        havingContainer.comparisionOperator, havingContainer.value);

				if (havingContainer.notOperator != null)
					predicate = ComparisonOperatorProcessor.negate(
					        criteriaBuilder, predicate);

				LogicOperator nextOperator = havingContainer.logicOperator;

				if (nextOperator == LogicOperator.NONE
				        && !nextOperator.equals(lastOperator))
					nextOperator = lastOperator;

				switch (nextOperator)
				{
				case AND:
				case NONE:
					andPredicates.add(predicate);
					break;
				case OR:
					orPredicates.add(predicate);
					break;
				default:
					throw new java.lang.IllegalStateException(
					        "Unknown operator found "
					                + havingContainer.comparisionOperator.toString());
				}

				lastOperator = nextOperator;

			}// end for

			if (andPredicates != null && andPredicates.size() > 0)
				out.add(criteriaBuilder.and(andPredicates
				        .toArray(new Predicate[andPredicates.size()])));

			if (orPredicates != null && orPredicates.size() > 0)
				out.add(criteriaBuilder.or(orPredicates
				        .toArray(new Predicate[orPredicates.size()])));

		}// end if
	}

	/**
	 * Creates all Singular or Set _joinContainer for this entity and all the
	 * associated entities.
	 * 
	 * @param root
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void  generateJoins(final From root)
	{
		this.root = root;
		Join<E, ?> join = null;
		
		if (_joins != null && _joins.size() > 0)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> joinEntry : allSetJoins)
			{
				JoinContainer<E> joinContainer = joinEntry.getKey();

				if (joinContainer.setAttribute != null)
					join = root.join(joinContainer.setAttribute,
					        joinContainer.joinType);

				else
					if (joinContainer.singularAttribute != null)
						join = root.join(joinContainer.singularAttribute,
						        joinContainer.joinType);

				CriteriaComposer<?> subCriteria = joinEntry.getValue();

				if (subCriteria != null)
					subCriteria.generateJoins(join);
			}
		}
	}

	/**
	 * @param criteriaBuilder
	 * @param out
	 */
	@SuppressWarnings("unchecked")
	protected void generateOrderBy(final CriteriaBuilder criteriaBuilder,
	        Map<Integer, Order> out)
	{
		log.trace("generateOrderBy");

		Preconditions.checkNotNull(out);
		Preconditions.checkNotNull(root);
		Preconditions.checkNotNull(criteriaBuilder);

		// First others
		if (_joins != null)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> join : allSetJoins)
			{
				CriteriaComposer<?> subCriteria = join.getValue();

				if (subCriteria != null)
					subCriteria.generateOrderBy(criteriaBuilder, out);
			}
		}

		// Then me. I will overwrite rank of others.
		if (_orderBys != null && _orderBys.size() > 0)
		{
			for (OrderByContainer<E> orderByContainer : _orderBys)
			{
				boolean ascending = orderByContainer.ascending;

				if (ascending)
				{
					if (log.isDebugEnabled())
						log.debug("ascending _orderBy "
						        + orderByContainer.attribute.getName());
					out.put(orderByContainer.rank, criteriaBuilder.asc(root
					        .get(orderByContainer.attribute)));
				}
				else
				{
					if (log.isDebugEnabled())
						log.debug("descending _orderBy "
						        + orderByContainer.attribute.getName());
					out.put(orderByContainer.rank, criteriaBuilder.desc(root
					        .get(orderByContainer.attribute)));
				}
			}
		}

	}

	/**
	 * @param out
	 */
	@SuppressWarnings("unchecked")
	protected void generateSelect(final CriteriaBuilder criteriaBuilder,
	        final List<Selection<?>> out)
	{
		Preconditions.checkNotNull(out);

		if (multiselect !=null && multiselect.size()>0)
		{
			out.addAll(multiselect);
		}
		
		if (_selects != null && _selects.size() > 0)
		{
			for (SelectContainer<E> selectContainer : _selects)
			{
				if (selectContainer.function == null)
				{
					Selection<?> selection = root
					        .get(selectContainer.singularAttribute);
					selection.alias(selectContainer.alias);
					out.add(selection);
				}
				else
				{
					@SuppressWarnings("rawtypes")
                    Expression numExp=null;
					switch (selectContainer.function)
					{
					case COUNT:
						 numExp = criteriaBuilder.count(root
						        .get(selectContainer.singularAttribute));
						numExp.alias(selectContainer.alias);
						break;

					case AVG:
						 numExp = criteriaBuilder.avg(root
						        .get(selectContainer.singularAttribute));
						 numExp.alias(selectContainer.alias);
						break;

					case SUM:
						numExp = criteriaBuilder.sum(root
						        .get(selectContainer.singularAttribute));
						numExp.alias(selectContainer.alias);
						break;

					case MAX:
						numExp = criteriaBuilder.max(root
						        .get(selectContainer.singularAttribute));
						numExp.alias(selectContainer.alias);
						break;

					case MIN:
						numExp = criteriaBuilder.min(root
						        .get(selectContainer.singularAttribute));
						numExp.alias(selectContainer.alias);
						break;
					}
					
					if (numExp !=null)
						out.add(numExp);
				}

			}
		}

		if (_joins != null)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> join : allSetJoins)
			{
				CriteriaComposer<?> subCriteria = join.getValue();

				if (subCriteria != null)
					subCriteria.generateSelect(criteriaBuilder, out);
			}
		}
	}

	/**
	 * Adds all attributes to match and their values to the predicate.
	 * 
	 * @param criteriaBuilder
	 * @param out
	 *            - predicates
	 */
	@SuppressWarnings("unchecked")
	protected void generateWhere(final CriteriaBuilder criteriaBuilder,
	        final List<Predicate> out)
	{
		// log.debug("generateWhereClaus :");

		Preconditions.checkNotNull(out);
		Preconditions.checkNotNull(criteriaBuilder);
		Preconditions.checkNotNull(root);

		if (_joins != null && _joins.size() > 0)
		{
			Set<Entry<JoinContainer<E>, CriteriaComposer<?>>> allSetJoins = _joins
			        .entrySet();

			for (Entry<JoinContainer<E>, CriteriaComposer<?>> joinEntry : allSetJoins)
			{
				CriteriaComposer<?> subCriteria = joinEntry.getValue();

				if (subCriteria != null)
					subCriteria.generateWhere(criteriaBuilder, out);
			}
		}
		
		if (_wheres != null)
		{
			List<Predicate> andPredicates = new ArrayList<Predicate>(0);
			List<Predicate> orPredicates = new ArrayList<Predicate>(0);

			LogicOperator lastOperator = LogicOperator.NONE;

			for (WhereContainer<E> where : _wheres)
			{
				Predicate predicate = ComparisonOperatorProcessor.process(
				        criteriaBuilder, root.get(where.attribute),
				        where.comparisionOperator, where.value);

				if (where.notOperator != null)
					predicate = criteriaBuilder.not(predicate);

				LogicOperator nextOperator = where.logicOperator;

				if (nextOperator == LogicOperator.NONE
				        && !nextOperator.equals(lastOperator))
					nextOperator = lastOperator;

				switch (nextOperator)
				{
					case AND:
					case NONE:
						andPredicates.add(predicate);
					break;
					case OR:
						orPredicates.add(predicate);
					break;
					default:
						throw new java.lang.IllegalStateException(
					        "Unknown operator found "
					                + where.comparisionOperator.toString());
				}

				lastOperator = nextOperator;

			}// end for
			
			if (andPredicates != null && andPredicates.size() > 0)
				out.add(criteriaBuilder.and(andPredicates
				        .toArray(new Predicate[andPredicates.size()])));

			if (orPredicates != null && orPredicates.size() > 0)
				out.add(criteriaBuilder.or(orPredicates
				        .toArray(new Predicate[orPredicates.size()])));

		}// end if
	}

	public Class<E> getEntityClass()
	{
		return this._entityClass;
	}

	/**
	 * Get the sub criteria by going through joins.
	 * 
	 * @return
	 */
/*	public CriteriaComposer<?>[] getSubCriterias()
	{
		if (_joins != null)
		{
			CriteriaComposer<?>[] out = new CriteriaComposer<?>[_joins.size()];
			_joins.values().toArray(out);
			return out;
		}
		else
			return new CriteriaComposer<?>[0];
		
		
		//tbd
		//call recursively
	}*/
	
	/**
	 * Get the sub criteria by going through joins.
	 * 
	 * @return
	 */
/*	public <T> CriteriaComposer<T> getSubCriteria(Class<T> forClass)
	{
		
		//tbd
		//????
		return null;
	
	}	*/

	/**
	 * Example:
	 * CriteriaComposer.createComposer(Person.class).select(Person_.name
	 * ).groupBy(Person_.name);
	 * forStudentUnitCount.join(Person_.courseSessions).
	 * join(CourseSession_.course).select(SUM, Course_.unit);
	 * 
	 * @param attribute
	 * @return
	 */
	public <V> CriteriaComposer<E> groupBy(SingularAttribute<? super E, V> attribute)
	{
		return this.groupBy(attribute, 1);
	}

	public <V> CriteriaComposer<E> groupBy(SingularAttribute<? super E, V> ...attributes )
	{

		for (int i=0; i < attributes.length; i++)
			this.groupBy(attributes[i], i);
		
		return this;
	}
	
	
	/**
	 * 
	 * @param attribute
	 * @param rank
	 *            - precedence order.
	 * @return
	 */
	public <V> CriteriaComposer<E> groupBy(SingularAttribute<? super E, V> attribute,
	        int rank)
	{
		Preconditions.checkNotNull(attribute);
		Preconditions.checkArgument(_selects.size()>0,"Please use select clause first.");

		GroupByContainer<E> groupByContainer = new GroupByContainer<E>(
		        attribute, rank);

		int index = this._groupBys.indexOf(groupByContainer);

		// overwrite
		if (index >= 0)
			this._groupBys.add(index, groupByContainer);
		else
			this._groupBys.add(groupByContainer);

		return this;
	}

	/**
	 * Adds attribute and its value to having clause and adds logic operation
	 * for the next having clause.
	 * 
	 * @param function
	 *            - {@link AggregateFunction}
	 * @param attribute
	 *            - SingularAttribute of the entity for which this search was
	 *            created.
	 * @param cOperator
	 *            - {@link ComparisonOperator}
	 * @param value
	 *            - value to match with the attribute.
	 * @return
	 */
/*	public <V> CriteriaComposer<E> having(AggregateFunction function,
	        SingularAttribute<E, V> attribute, ComparisonOperator cOperator,
	        V value)
	{
		return this.having(function, attribute, cOperator, value,
		        LogicOperator.NONE);
	}*/

	/**
	 * Adds the attribute to having clause of the entity
	 * 
	 * @param function
	 *            - {@link AggregateFunction}
	 * @param attribute
	 *            - SingularAttribute of the entity for which this search was
	 *            created.
	 * @param cOperator
	 *            - {@link ComparisonOperator}
	 * @param value
	 *            - value to match with the attribute.
	 * @param lOperator
	 *            - {@link LogicOperator} logic operator that will be applied
	 *            with next having.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public <V> CriteriaComposer<E> having(AggregateFunction function,
	        SingularAttribute<? super E, V> attribute, ComparisonOperator cOperator,
	        V value)
	{
		Preconditions.checkArgument(_selects.indexOf(new SelectContainer(attribute,"")) >= 0, "Unable to find related select clasue." );
		
		Preconditions.checkNotNull(function);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(cOperator);
		
		HavingContainer<E> havingContainer = new HavingContainer<E>(function,
		        attribute, cOperator, value);

		int index = _havings.indexOf(havingContainer);

		if (index >= 0)
			_havings.add(index, havingContainer);
		else
			_havings.add(havingContainer);

		log.debug("Adding having " + havingContainer.toString());

		lastCallType = LastCallType.HAVING;
		return this;
	}

	/**
	 * Adds attribute and its value to having clause and adds logic operation
	 * for the next having clause.
	 * 
	 * @param function
	 *            - {@link AggregateFunction}
	 * @param attribute
	 *            - SingularAttribute of the entity for which this search was
	 *            created.
	 * @param nOperator
	 *            - {@link NegationOperator}
	 * @param cOperator
	 *            - {@link ComparisonOperator}
	 * @param value
	 *            - value to match with the attribute.
	 * @return
	 */
/*	public <V> CriteriaComposer<E> having(AggregateFunction function,
	        SingularAttribute<? super E, V> attribute, NegationOperator nOperator,
	        ComparisonOperator cOperator, V value)
	{
		return this.having(function, attribute, nOperator, cOperator, value,
		        LogicOperator.NONE);
	}*/

	/**
	 * Adds the attribute to having clause of the entity
	 * 
	 * @param function
	 *            - {@link AggregateFunction}
	 * @param attribute
	 *            - SingularAttribute of the entity for which this search was
	 *            created.
	 * @param nOperator
	 *            - {@link NegationOperator}
	 * @param cOperator
	 *            - {@link ComparisonOperator}
	 * @param value
	 *            - value to match with the attribute.
	 * @param lOperator
	 *            - {@link LogicOperator} logic operator that will be applied
	 *            with next having.
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public <V> CriteriaComposer<E> having(AggregateFunction function,
	        SingularAttribute<? super E, V> attribute, NegationOperator nOperator,
	        ComparisonOperator cOperator, V value)
	{

		Preconditions.checkArgument(_selects.indexOf(new SelectContainer(attribute,"")) >= 0, "Unable to find related select clasue." );
		
		Preconditions.checkNotNull(function);
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(nOperator);
		Preconditions.checkNotNull(cOperator);
		
		HavingContainer<E> havingContainer = new HavingContainer<E>(function,
				attribute, nOperator, cOperator, value);

		int index = _havings.indexOf(havingContainer);

		if (index >= 0)
			_havings.add(index, havingContainer);
		else
			_havings.add(havingContainer);

		log.debug("Adding having for " + attribute.getName() + " "
		        + cOperator.toString() + " value " + value.toString());

		lastCallType = LastCallType.HAVING;
		
		return this;
	}

	/**
	 * Creates a set join with the next entity.
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SetAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next entity
	 */
	public <V> CriteriaComposer<V> join(JoinType joinType,
	        SetAttribute<E, V> attribute)
	{
		return join(joinType, attribute, null);
	}

	/**
	 * Creates a set join with the next entity.
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SetAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next entity
	 */
	@SuppressWarnings("unchecked")
    public <V> CriteriaComposer<V> join(JoinType joinType,
	        SetAttribute<? super E, V> attribute, CriteriaComposer<V> subCriteria)
	{
		Preconditions.checkNotNull(attribute);

		Class<V> classToJoin = attribute.getBindableJavaType();

		JoinContainer<E> join = new JoinContainer<E>(joinType, attribute);

		// Dont overwrite join
		if (_joins.containsKey(join))
			return (CriteriaComposer<V>) _joins.get(join);

		if (subCriteria == null)
			subCriteria = new CriteriaComposer<V>(classToJoin);

		_joins.put(join, subCriteria);

		log.debug("Addeding join " + joinType.toString() + " on "
		        + classToJoin.getSimpleName() + " " + attribute.getName());

		return subCriteria;
	}

	/**
	 * Addes attribute for orderBy clause in ascending. Auto increments the rank
	 * from left to right.
	 * 
	 * @param attributes
	 * @return
	 * 
	 *         public CriteriaContainer<E> orderBy(SingularAttribute<E, ?> ...
	 *         attributes) {
	 * 
	 *         Preconditions.checkNotNull(attributes); int rank=1; for
	 *         (SingularAttribute<E, ?> attribute: attributes) {
	 *         log.debug("Adding _orderBy for " + attribute.getName());
	 *         _orderBy.add(new OrderByContainer<E>(attribute, true, rank));
	 *         rank++; }
	 * 
	 *         return this; }
	 */

	/**
	 * Creates a singular join with the next entity.
	 * 
	 * @param <R>
	 * @param attribute
	 *            - SingularAttribute of this entity that has reference to next
	 *            entity
	 * @return - Sub criteria for the next entity
	 */
	public <V> CriteriaComposer<V> join(JoinType joinType,
	        SingularAttribute<? super E, V> attribute)
	{
		return join(joinType, attribute, null);
	}

	/**
	 * Creates a singular join with the next entity.
	 * 
	 * @param <R>
	 *            - The entity type to join with. (right side)
	 * @param attribute
	 *            - SingularAttribute of this entity that has reference to next
	 *            entity
	 * @return - Sub criteria for the next entity
	 */
	@SuppressWarnings("unchecked")
    public <V> CriteriaComposer<V> join(JoinType joinType,
	        SingularAttribute<? super E, V> attribute, CriteriaComposer<V> subCriteria)
	{
		Preconditions.checkNotNull(attribute);

		Class<V> classToJoin = attribute.getBindableJavaType();

		JoinContainer<E> join = new JoinContainer<E>(joinType, attribute);

		// Don't overwrite join
		if (_joins.containsKey(join))
			return (CriteriaComposer<V>) _joins.get(join);

		if (subCriteria == null)
			subCriteria = new CriteriaComposer<V>(classToJoin);

		_joins.put(join, subCriteria);

		log.debug("Addeding join " + joinType.toString() + " on "
		        + classToJoin.getSimpleName() + " " + attribute.getName());

		return subCriteria;
	}

	/**
	 * @param _selectContainer
	 * 
	 *            public CriteriaContainer<E> groupBy(SingularAttribute<E, ?>...
	 *            attributes) { int rank=1; for (SingularAttribute<E, ?>
	 *            attribute : attributes) { GroupByContainer<E> groupBy = new
	 *            GroupByContainer<E>(attribute, rank);
	 *            this._groupBy.add(groupBy); rank++; } return this; }
	 */

	/**
	 * Creates a set join with the next entity. Uses JoinType.INNER as default
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SetAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next joined entity
	 */
	public <V> CriteriaComposer<V> join(SetAttribute<? super E, V> attribute)
	{
		return join(JoinType.INNER, attribute, null);
	}

	/**
	 * Creates a set join with the next entity. Users JoinType.INNER as default.
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SetAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next entity
	 */
	public <V> CriteriaComposer<V> join(SetAttribute<E, V> attribute,
	        CriteriaComposer<V> subCriteria)
	{
		return join(JoinType.INNER, attribute, subCriteria);
	}

	/**
	 * Creates a set join with the next entity. Uses JoinType.INNER as default
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SingularAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next joined entity
	 */
	public <V> CriteriaComposer<V> join(SingularAttribute<? super E, V> attribute)
	{
		return join(JoinType.INNER, attribute, null);
	}

	/**
	 * Creates a set join with the next entity. Users JoinType.INNER as default.
	 * 
	 * @param <R>
	 *            - Class of the next entity
	 * @param attribute
	 *            - SingularAttribute of this entity that has reference to next
	 *            entity
	 * @return Sub criteria for the next joined entity
	 */
	public <V> CriteriaComposer<V> join(SingularAttribute<? super E, V> attribute,
	        CriteriaComposer<V> subCriteria)
	{
		return join(JoinType.INNER, attribute, subCriteria);
	}
	
	
	/**
	 * Ascending orderBy.
	 * @param attributes
	 * @return
	 */
	public <V> CriteriaComposer<E> orderBy(SingularAttribute<? super E, V> ...attributes)
	{
		for (int i=0; i < attributes.length; i++)
			this.orderBy(attributes[i], true, i);
		
		return this;
	}	

	/**
	 * @param attribute
	 * @param ascending
	 * @param rank
	 *            - precedence order - should be greater then zero and should be
	 *            unique across the entire graph. Subsequent occurrences of the
	 *            attribute with the same ranked number will be ignored and will
	 *            not be added into orderBy clause.
	 * 
	 */
	public <V> CriteriaComposer<E> orderBy(SingularAttribute<? super E, V> attribute,
	        boolean ascending, int rank)
	{

		Preconditions.checkArgument(rank > 0);
		Preconditions.checkNotNull(attribute);

		log.debug("Adding _orderBy for " + attribute.getName());

		OrderByContainer<E> orderByContainer = new OrderByContainer<E>(
		        attribute, ascending, rank);

		int index = _orderBys.indexOf(orderByContainer);

		if (index > -0)
			_orderBys.add(index, orderByContainer);
		else
			_orderBys.add(orderByContainer);

		return this;
	}

	/**
	 * @param function
	 * @param attribute
	 * @return
	 */
	public <V>CriteriaComposer<E> select(AggregateFunction function,
	        SingularAttribute<? super E, V> attribute)
	{
		Preconditions.checkNotNull(function);
		Preconditions.checkNotNull(attribute);

		return this.select(function, attribute, "");
	}

	/**
	 * @param function
	 * @param attribute
	 * @param alias
	 * @return
	 */
	public <V> CriteriaComposer<E> select(AggregateFunction function,
	        SingularAttribute<? super E, V> attribute, String alias)
	{
		Preconditions.checkNotNull(function);
		Preconditions.checkNotNull(attribute);

		if (alias == null || alias.trim().length() == 0)
			alias = function.toString().toLowerCase() + "."
			        + this._entityClass.getSimpleName() + "."
			        + attribute.getName();

		SelectContainer<E> selectContainer = new SelectContainer<E>(function,
		        attribute, alias);

		int index = _selects.indexOf(selectContainer);

		if (index >= 0)
			_selects.add(index, selectContainer);
		else
			_selects.add(selectContainer);

		log.debug("Added select " + attribute.toString());

		return this;
	}

	/**
	 * @param attribute
	 * @return
	 */
	public <V> CriteriaComposer<E> select(SingularAttribute<? super E, V> attribute)
	{
		return this.select(attribute, "");
	}

	/**
	 * @param attributesToSelect
	 * @return
	 */
	public  CriteriaComposer<E> select(
	        SingularAttribute<? super E, ?>... attributesToSelect)
	{

		for (SingularAttribute<? super E, ?> attribute : attributesToSelect)
		{
			this.select(attribute, "");
		}

		return this;
	}

	/**
	 * @param attribute
	 * @param alias
	 */
	public <V> CriteriaComposer<E> select(SingularAttribute<? super E, V> attribute,
	        String alias)
	{
		Preconditions.checkNotNull(attribute);

		if (alias == null || alias.trim().length() == 0)
			alias = this._entityClass.getSimpleName() + "."
			        + attribute.getName();

		SelectContainer<E> selectContainer = new SelectContainer<E>(attribute,
		        alias);

		// overwrite if exists.
		int index = _selects.indexOf(selectContainer);

		if (index >= 0)
			_selects.add(index, selectContainer);
		else
			_selects.add(selectContainer);

		log.debug("Added select for " + attribute.getName() + "with alias "
		        + alias);

		return this;
	}

	@Override
	public String toString()
	{
		return "CriteriaContainer [root=" + _entityClass.getSimpleName() + "]";
	}

	/**
	 * for IN and BETWEEN
	 * @param attribute
	 * @param cOperator
	 * @param values
	 * @return
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, List<V> values)
	{
		return where(attribute, cOperator, values, LogicOperator.NONE);
	}*/

	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param cOperator
	 * @param values
	 * @param lOperator
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, List<V> values)
	{
		Preconditions.checkArgument(cOperator
		        .equals(ComparisonOperator.BETWEEN)
		        || cOperator.equals(ComparisonOperator.IN));
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(cOperator);


		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        cOperator, values);

		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " value count " + values.size());

		lastCallType = LastCallType.WHERE;
		
		return this;
	}

	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param cOperator
	 * @param values
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, V... values)
	{
		return where(attribute, cOperator,
		        new ArrayList<V>(Arrays.asList(values)));
	}

	/**
	 * Adds attribute and its value to where clause and adds logic operation for
	 * the next where clause.
	 * 
	 * @param attribute
	 * @param operator
	 * @param value
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator operator, V value)
	{
		return this.where(attribute, operator, value, LogicOperator.NONE);
	}*/

	/**
	 * Adds the attribute to where clause of the entity
	 * 
	 * @param attribute
	 * @param cOperator
	 * @param value
	 * @param lOperator
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, V value)
	{
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(cOperator);

		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        cOperator, value);

		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " value " + value.toString()
		        );

		lastCallType = LastCallType.WHERE;
		
		return this;
	}

	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param values
	 * @return
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator,
	        List<V> values)
	{
		return where(attribute, nOperator, cOperator, values,
		        LogicOperator.NONE);
	}*/

	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param values
	 * @param lOperator
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator,
	        List<V> values)
	{
		Preconditions.checkArgument(cOperator
		        .equals(ComparisonOperator.BETWEEN)
		        || cOperator.equals(ComparisonOperator.IN));

		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(nOperator);
		Preconditions.checkNotNull(cOperator);

		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        nOperator, cOperator, values);

		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " value count " + values.size());

		lastCallType = LastCallType.WHERE;
		
		return this;
	}

	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param values
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator,
	        V... values)
	{
		return where(attribute, nOperator, cOperator,
		        new ArrayList<V>(Arrays.asList(values)));
	}

	/**
	 * Adds attribute and its value to where clause and adds logic operation for
	 * the next where clause.
	 * 
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param value
	 * @return
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator, V value)
	{
		return this.where(attribute, nOperator, cOperator, value);
	}*/

	/**
	 * Adds the attribute to where clause of the entity
	 * 
	 * @param attribute
	 *            - SingularAttribute of the entity for which this search was
	 *            created.
	 * @param value
	 *            - value to match with the attribute.
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator, V value)
	{
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(nOperator);
		Preconditions.checkNotNull(cOperator);

		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        nOperator, cOperator, value);

		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " value " + value.toString());

		lastCallType = LastCallType.WHERE;
		
		return this;
	}
	
	
	/***********************************************************************************************/
	/**
	 * for IN and BETWEEN
	 * @param attribute
	 * @param cOperator
	 * @param values
	 * @return
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, CriteriaComposer<V> subCriteria)
	{
		return where(attribute, cOperator, subCriteria, LogicOperator.NONE);
	}*/


	public <V> CriteriaComposer<E> and()
	{
		switch (lastCallType)
		{
			case WHERE:
				if (this._wheres!=null && this._wheres.size()>0)
				{
					this._wheres.get(this._wheres.size()-1).logicOperator=LogicOperator.AND;
				}
				break;
			
			case HAVING:
				if (this._havings!=null && this._havings.size()>0)
				{
					this._havings.get(this._havings.size()-1).logicOperator=LogicOperator.AND;
				}
				break;
		}
		
		return this;
	}
	
	public <V> CriteriaComposer<E> or()
	{
		switch (lastCallType)
		{
			case WHERE:
				if (this._wheres!=null && this._wheres.size()>0)
				{
					this._wheres.get(this._wheres.size()-1).logicOperator=LogicOperator.OR;
				}
				break;
			
			case HAVING:
				if (this._havings!=null && this._havings.size()>0)
				{
					this._havings.get(this._havings.size()-1).logicOperator=LogicOperator.OR;
				}
				break;
			
		}
		
		return this;
	}	
	
	/**
	 * @param attribute
	 * @param cOperator
	 * @param subCriteria
	 * @param lOperator
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        ComparisonOperator cOperator, CriteriaComposer<V> subCriteria)
	{
		Preconditions.checkArgument(cOperator.equals(ComparisonOperator.IN));
		
		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(cOperator);

		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        cOperator, subCriteria);
		
		whereContainer.logicOperator = LogicOperator.AND;
		
		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)//overwrite
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " sub criteria " + subCriteria);

		lastCallType = LastCallType.WHERE;
		
		return this;
	}


	/**
	 * for IN and BETWEEN
	 * 
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param values
	 * @return
	 */
/*	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator,
	        CriteriaComposer<V> subCriteria)
	{
		return where(attribute, nOperator, cOperator, subCriteria,
		        LogicOperator.NONE);
	}*/


	/**
	 * @param attribute
	 * @param nOperator
	 * @param cOperator
	 * @param subCriteria
	 * @param lOperator
	 * @return
	 */
	public <V> CriteriaComposer<E> where(SingularAttribute<? super E, V> attribute,
	        NegationOperator nOperator, ComparisonOperator cOperator,
	        CriteriaComposer<V> subCriteria)
	{
		Preconditions.checkArgument(cOperator.equals(ComparisonOperator.IN));

		Preconditions.checkNotNull(attribute);
		Preconditions.checkNotNull(nOperator);
		Preconditions.checkNotNull(cOperator);

		WhereContainer<E> whereContainer = new WhereContainer<E>(attribute,
		        nOperator, cOperator, subCriteria);

		int index = _wheres.indexOf(whereContainer);

		if (index >= 0)
			_wheres.add(index, whereContainer);
		else
			_wheres.add(whereContainer);

		log.debug("Adding where for " + attribute.getName() + " "
		        + cOperator.toString() + " sub criteria " + subCriteria);

		
		lastCallType = LastCallType.WHERE;
		
		return this;
	}
	

	/**
	 *
	 * @return
	 */
	public From getFrom()
    {
		return this.root;
	    
    }

	/**
	 * Added the JPA Selection objects as it is to internal collection. 
	 * GenerateSelect then copied to the final list, which is then used by 
	 * CriteriaProcessor to select particular columns of the entity(ies).
	 *   
	 *	 @see {@link javax.persistence.criteria.Selection}
	 *  
	 * @param selectedColumns
	 */
	public void select(List<Selection<?>> selectedColumns)
    {
	    this.multiselect = selectedColumns;
    }
	
/*	protected void generateSubQuery(CriteriaBuilder cb, CriteriaQuery cq)
	{
		//TBD cq.subquery(arg0)
	}*/
	
}
