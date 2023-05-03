package com.wuav.client.dal.mappers;

import com.wuav.client.be.CustomImage;
import com.wuav.client.be.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectMapper {

    List<Project> getAllProjectsByUserId(@Param("userId")int userId);

    Project getProjectById(@Param("id")int id);

    // inserting project to the table
    int createProjectByName(@Param("id")int id,@Param("name")String name,@Param("status") String status);


    void addUserToProject(@Param("userId") int userId,@Param("projectId") int projectId);

    // updating project with description
    int updateProjectForUserById(@Param("projectId")int projectId,@Param("description") String description);

}
