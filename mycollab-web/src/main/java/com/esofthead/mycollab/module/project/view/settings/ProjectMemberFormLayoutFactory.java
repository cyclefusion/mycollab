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

package com.esofthead.mycollab.module.project.view.settings;

import com.esofthead.mycollab.module.project.i18n.ProjectMemberI18nEnum;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.ui.GridFormLayoutHelper;
import com.esofthead.mycollab.vaadin.ui.IFormLayoutFactory;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author MyCollab Ltd.
 * @since 1.0
 */
public class ProjectMemberFormLayoutFactory implements IFormLayoutFactory {
	private static final long serialVersionUID = 1L;
	private GridFormLayoutHelper informationLayout;

	@Override
	public ComponentContainer getLayout() {
		final VerticalLayout layout = new VerticalLayout();
		final Label organizationHeader = new Label(
				AppContext
						.getMessage(ProjectMemberI18nEnum.FORM_INFORMATION_SECTION));
		organizationHeader.setStyleName("h2");
		layout.addComponent(organizationHeader);

		this.informationLayout = GridFormLayoutHelper.defaultFormLayoutHelper(1, 2);
		layout.addComponent(this.informationLayout.getLayout());
		return layout;
	}

	@Override
	public void attachField(final Object propertyId, final Field<?> field) {
		if (propertyId.equals("username")) {
			this.informationLayout.addComponent(field,
					AppContext.getMessage(ProjectMemberI18nEnum.FORM_USER), 0,
					0);
		} else if (propertyId.equals("projectroleid")) {
			this.informationLayout.addComponent(field,
					AppContext.getMessage(ProjectMemberI18nEnum.FORM_ROLE), 0,
					1);
		}
	}
}
