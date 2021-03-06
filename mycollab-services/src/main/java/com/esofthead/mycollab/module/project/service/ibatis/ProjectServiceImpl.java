/**
 * This file is part of mycollab-services.
 *
 * mycollab-services is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-services is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-services.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.service.ibatis;

import com.esofthead.mycollab.common.ModuleNameConstants;
import com.esofthead.mycollab.common.i18n.OptionI18nEnum.StatusI18nEnum;
import com.esofthead.mycollab.common.interceptor.aspect.ClassInfo;
import com.esofthead.mycollab.common.interceptor.aspect.ClassInfoMap;
import com.esofthead.mycollab.common.interceptor.aspect.Traceable;
import com.esofthead.mycollab.configuration.SiteConfiguration;
import com.esofthead.mycollab.core.DeploymentMode;
import com.esofthead.mycollab.core.UserInvalidInputException;
import com.esofthead.mycollab.core.arguments.NumberSearchField;
import com.esofthead.mycollab.core.arguments.SetSearchField;
import com.esofthead.mycollab.core.arguments.StringSearchField;
import com.esofthead.mycollab.core.cache.CacheKey;
import com.esofthead.mycollab.core.persistence.ICrudGenericDAO;
import com.esofthead.mycollab.core.persistence.ISearchableDAO;
import com.esofthead.mycollab.core.persistence.service.DefaultService;
import com.esofthead.mycollab.esb.CamelProxyBuilderUtil;
import com.esofthead.mycollab.module.billing.service.BillingPlanCheckerService;
import com.esofthead.mycollab.module.project.ProjectMemberStatusConstants;
import com.esofthead.mycollab.module.project.ProjectRolePermissionCollections;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.dao.ProjectMapper;
import com.esofthead.mycollab.module.project.dao.ProjectMapperExt;
import com.esofthead.mycollab.module.project.dao.ProjectMemberMapper;
import com.esofthead.mycollab.module.project.domain.*;
import com.esofthead.mycollab.module.project.domain.criteria.ProjectSearchCriteria;
import com.esofthead.mycollab.module.project.esb.DeleteProjectCommand;
import com.esofthead.mycollab.module.project.esb.ProjectEndPoints;
import com.esofthead.mycollab.module.project.service.ProjectRoleService;
import com.esofthead.mycollab.module.project.service.ProjectService;
import com.esofthead.mycollab.module.project.service.ProjectTaskListService;
import com.esofthead.mycollab.security.AccessPermissionFlag;
import com.esofthead.mycollab.security.PermissionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
@Service
@Transactional
@Traceable(nameField = "name", extraFieldName = "id")
public class ProjectServiceImpl extends
        DefaultService<Integer, Project, ProjectSearchCriteria> implements
        ProjectService {

    static {
        ClassInfoMap.put(ProjectServiceImpl.class, new ClassInfo(ModuleNameConstants.PRJ, ProjectTypeConstants.PROJECT));
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMapperExt projectMapperExt;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private ProjectTaskListService taskListService;

    @Autowired
    private BillingPlanCheckerService billingPlanCheckerService;

    @SuppressWarnings("unchecked")
    @Override
    public ICrudGenericDAO<Integer, Project> getCrudMapper() {
        return projectMapper;
    }

    @Override
    public ISearchableDAO<ProjectSearchCriteria> getSearchMapper() {
        return projectMapperExt;
    }

    @Override
    public int updateWithSession(Project record, String username) {
        assertExistProjectShortnameInAccount(record.getId(), record.getShortname(),
                record.getSaccountid());
        return super.updateWithSession(record, username);
    }

    @Override
    public int saveWithSession(Project record, String username) {
        billingPlanCheckerService.validateAccountCanCreateMoreProject(record
                .getSaccountid());

        assertExistProjectShortnameInAccount(null, record.getShortname(),
                record.getSaccountid());

        int projectId = super.saveWithSession(record, username);

        // Add the first user to project
        ProjectMember projectMember = new ProjectMember();
        projectMember.setIsadmin(Boolean.TRUE);
        projectMember.setStatus(ProjectMemberStatusConstants.ACTIVE);
        projectMember.setJoindate(new GregorianCalendar().getTime());
        projectMember.setProjectid(projectId);
        projectMember.setUsername(username);
        projectMember.setSaccountid(record.getSaccountid());
        projectMemberMapper.insert(projectMember);

        // add client role to project
        ProjectRole clientRole = createProjectRole(projectId,
                record.getSaccountid(), "Client", "Default role for client");

        int clientRoleId = projectRoleService.saveWithSession(clientRole,
                username);

        PermissionMap permissionMapClient = new PermissionMap();
        for (int i = 0; i < ProjectRolePermissionCollections.PROJECT_PERMISSIONS.length; i++) {

            String permissionName = ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i];

            if (permissionName.equals(ProjectRolePermissionCollections.USERS)
                    || permissionName
                    .equals(ProjectRolePermissionCollections.ROLES)
                    || permissionName
                    .equals(ProjectRolePermissionCollections.MESSAGES)) {
                permissionMapClient
                        .addPath(
                                ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i],
                                AccessPermissionFlag.NO_ACCESS);
            } else {
                permissionMapClient
                        .addPath(
                                ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i],
                                AccessPermissionFlag.READ_ONLY);
            }
        }
        projectRoleService.savePermission(projectId, clientRoleId,
                permissionMapClient, record.getSaccountid());

        // add consultant role to project
        LOG.debug("Add consultant role to project {}", record.getName());
        ProjectRole consultantRole = createProjectRole(projectId,
                record.getSaccountid(), "Consultant",
                "Default role for consultant");
        int consultantRoleId = projectRoleService.saveWithSession(
                consultantRole, username);

        PermissionMap permissionMapConsultant = new PermissionMap();
        for (int i = 0; i < ProjectRolePermissionCollections.PROJECT_PERMISSIONS.length; i++) {

            String permissionName = ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i];

            if (permissionName.equals(ProjectRolePermissionCollections.USERS)
                    || permissionName
                    .equals(ProjectRolePermissionCollections.ROLES)) {
                permissionMapConsultant
                        .addPath(
                                ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i],
                                AccessPermissionFlag.READ_ONLY);
            } else {
                permissionMapConsultant
                        .addPath(
                                ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i],
                                AccessPermissionFlag.ACCESS);
            }
        }
        projectRoleService.savePermission(projectId, consultantRoleId,
                permissionMapConsultant, record.getSaccountid());

        // add admin role to project
        LOG.debug("Add admin role to project {}", record.getName());
        ProjectRole adminRole = createProjectRole(projectId,
                record.getSaccountid(), "Admin", "Default role for admin");
        int adminRoleId = projectRoleService.saveWithSession(adminRole,
                username);

        PermissionMap permissionMapAdmin = new PermissionMap();
        for (int i = 0; i < ProjectRolePermissionCollections.PROJECT_PERMISSIONS.length; i++) {

            permissionMapAdmin.addPath(
                    ProjectRolePermissionCollections.PROJECT_PERMISSIONS[i],
                    AccessPermissionFlag.ACCESS);
        }
        projectRoleService.savePermission(projectId, adminRoleId,
                permissionMapAdmin, record.getSaccountid());

        LOG.debug("Create default task group");
        TaskList taskList = new TaskList();
        taskList.setProjectid(projectId);
        taskList.setSaccountid(record.getSaccountid());
        taskList.setStatus(StatusI18nEnum.Open.name());
        taskList.setName("General Assignments");
        taskListService.saveWithSession(taskList, username);

        return projectId;
    }

    private void assertExistProjectShortnameInAccount(Integer projectId, String shortname,
                                                      int sAccountId) {
        ProjectExample ex = new ProjectExample();
        ProjectExample.Criteria criteria = ex.createCriteria();
        criteria.andShortnameEqualTo(shortname)
                .andSaccountidEqualTo(sAccountId);
        if (projectId != null) {
            criteria.andIdNotEqualTo(projectId);
        }
        if (projectMapper.countByExample(ex) > 0) {
            throw new UserInvalidInputException(
                    "There is already project in the account has short name "
                            + shortname);
        }
    }

    private ProjectRole createProjectRole(int projectId, int sAccountId,
                                          String roleName, String description) {
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProjectid(projectId);
        projectRole.setSaccountid(sAccountId);
        projectRole.setRolename(roleName);
        projectRole.setDescription(description);
        return projectRole;
    }

    @Override
    public SimpleProject findById(int projectId, int sAccountId) {
        return projectMapperExt.findProjectById(projectId);
    }

    @Override
    public List<Integer> getProjectKeysUserInvolved(String username,
                                                    Integer sAccountId) {
        ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
        searchCriteria.setInvolvedMember(new StringSearchField(username));
        searchCriteria.setProjectStatuses(new SetSearchField<>(
                new String[]{StatusI18nEnum.Open.name()}));
        return projectMapperExt.getUserProjectKeys(searchCriteria);
    }

    @Override
    public String getSubdomainOfProject(int projectId) {
        if (SiteConfiguration.getDeploymentMode() == DeploymentMode.site) {
            return projectMapperExt.getSubdomainOfProject(projectId);
        } else {
            return SiteConfiguration.getSiteUrl("");
        }
    }

    @Override
    public int removeWithSession(Integer projectId, String username,
                                 int accountId) {
        // notify listener project is removed, then silently remove project in
        // associate records
        try {
            Project project = findByPrimaryKey(projectId, accountId);

            DeleteProjectCommand projectDeleteListener = CamelProxyBuilderUtil
                    .build(ProjectEndPoints.PROJECT_REMOVE_ENDPOINT,
                            DeleteProjectCommand.class);
            projectDeleteListener.projectRemoved(project.getSaccountid(),
                    projectId);
        } catch (Exception e) {
            LOG.error("Error while notify user delete", e);
        }
        return super.removeWithSession(projectId, username, accountId);
    }

    @Override
    public Integer getTotalActiveProjectsInAccount(@CacheKey Integer sAccountId) {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setSaccountid(new NumberSearchField(sAccountId));
        criteria.setProjectStatuses(new SetSearchField<>(
                new String[]{StatusI18nEnum.Open.name()}));
        return projectMapperExt.getTotalCount(criteria);
    }

    @Override
    public List<ProjectRelayEmailNotification> findProjectRelayEmailNotifications() {
        return projectMapperExt.findProjectRelayEmailNotifications();
    }

    @Override
    public List<SimpleProject> getProjectsUserInvolved(String username,
                                                       Integer sAccountId) {
        return projectMapperExt.getProjectsUserInvolved(username, sAccountId);
    }
}
