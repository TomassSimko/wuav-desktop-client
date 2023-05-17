package com.wuav.client.dal.repository;

import com.wuav.client.be.Project;
import com.wuav.client.be.device.Device;
import com.wuav.client.dal.interfaces.IProjectRepository;
import com.wuav.client.dal.mappers.IDeviceMapper;
import com.wuav.client.dal.mappers.IProjectMapper;
import com.wuav.client.dal.myBatis.MyBatisConnectionFactory;
import com.wuav.client.gui.dto.CreateProjectDTO;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectRepository implements IProjectRepository {
    private static Logger logger = LoggerFactory.getLogger(ProjectRepository.class);

    @Override
    public List<Project> getAllProjects() throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            return mapper.getAllProjects();
        }catch(PersistenceException ex){
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }

    @Override
    public List<Project> getAllProjectsByUserId(int userId) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            return mapper.getAllProjectsByUserId(userId);
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }
    @Override
    public Project getProjectById(int projectId) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            return mapper.getProjectById(projectId);
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }


    @Override
    public Project updateProject(int projectId, String description) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            mapper.updateProjectForUserById(projectId, description);
            session.commit();
            return mapper.getProjectById(projectId);
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }

    @Override
    public boolean createProject(CreateProjectDTO projectDTO) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            var affectedRows = mapper.createProject(
                    projectDTO.id(),
                    projectDTO.name(),
                    projectDTO.description(),
                    projectDTO.customer().id()
            );
            session.commit();
            return affectedRows > 0;
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }

    @Override
    public int addProjectToUser(int userId, int projectId) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            session.commit();
            return mapper.addUserToProject(userId, projectId);
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }

    @Override
    public int addDeviceToProject(int projectId, int deviceId) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            session.commit();
            return mapper.addDeviceToProject(projectId, deviceId);
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }

    @Override
    public boolean updateNotes(int projectId, String content) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            var affectedRows = mapper.updateNotes(
                    projectId,
                    content
            );
            session.commit();
            return affectedRows > 0;
        } catch (PersistenceException ex) {
           throw new Exception(ex);
        }
    }

    @Override
    public boolean deleteProjectById(int id) throws Exception {
        try (SqlSession session = MyBatisConnectionFactory.getSqlSessionFactory().openSession()) {
            IProjectMapper mapper = session.getMapper(IProjectMapper.class);
            var affectedRows = mapper.deleteProjectById(
                  id
            );
            session.commit();
            return affectedRows > 0;
        } catch (PersistenceException ex) {
            logger.error("An error occurred mapping tables", ex);
            throw new Exception(ex);
        }
    }
}


