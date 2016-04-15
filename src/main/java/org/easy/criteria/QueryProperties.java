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

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryProperties 
{
	private static Log log = LogFactory.getLog(QueryProperties.class);
	
	String hintKey=null; 
	String hintValue=null;
	FlushModeType flashMode=null;
	LockModeType lockMode=null;
	
	int startIndex=-1;
	int maxResult=-1;
	
	public static QueryProperties getDefaultProperties()
	{
		return new QueryProperties();
	}

	public void setFlushMode(FlushModeType flushMode)
	{
		this.flashMode = flushMode;
	}
	
	public void setLockMode(LockModeType lockMode)
	{
		this.lockMode= lockMode;
	}
	
	public void setHint(String key, String value)
	{
		hintKey=key;
		hintValue=value;
	}
	
	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;		
	}
	
	public void setMaxResult(int maxResult)
	{
		this.maxResult = maxResult;		
	}
	
	public void applyProperties(Query query)
	{
		if (flashMode!=null)
		{
			log.debug("flashMode = " + flashMode);
			query.setFlushMode(flashMode);
		}
		
		if (lockMode!=null)
		{
			log.debug("lockMode = " + lockMode);
			query.setLockMode(lockMode);
		}
		
		if (hintKey!=null)
		{
			log.debug("hintKey = " + hintKey);
			log.debug("hintValue = " + hintValue);
			query.setHint(hintKey, hintValue);
		}
		
		if (startIndex>=0 && maxResult>0)
		{
			log.debug("startIndex = " + startIndex*maxResult);
			query.setFirstResult(startIndex*maxResult);
		}

		if (maxResult>0)
		{
			log.debug("maxResult = " + maxResult);
			query.setMaxResults(maxResult);
		}
	}
	
}
