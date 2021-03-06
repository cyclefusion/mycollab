/**
 * This file is part of mycollab-jackrabbit.
 *
 * mycollab-jackrabbit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-jackrabbit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-jackrabbit.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.ecm;

import com.esofthead.mycollab.core.MyCollabException;

/**
 * Generic exception relate to MyCollab storage processing.
 * 
 * @author MyCollab Ltd.
 * @since 1.0
 * 
 */
public class ContentException extends MyCollabException {
	private static final long serialVersionUID = 1L;

	public ContentException(String message) {
		super(message);
	}

}
