/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.user.accountsettings.view.events;

import com.esofthead.mycollab.eventmanager.ApplicationEvent;

/**
 * 
 * @author MyCollab Ltd.
 * @since 1.0
 * 
 */
public class ProfileEvent {
	
	public static class GotoProfileView extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public GotoProfileView(Object source, Object data) {
			super(source, data);
		}
	}
	
	public static class GotoProfileEdit extends ApplicationEvent {
		private static final long serialVersionUID = 1L;
		
		public GotoProfileEdit(Object source, Object data) {
			super(source, data);
		}
	}

	public static class GotoUploadPhoto extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public GotoUploadPhoto(Object source, Object data) {
			super(source, data);
		}
	}
}
