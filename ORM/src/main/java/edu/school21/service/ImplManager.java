package edu.school21.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface ImplManager {
    public void save(Object entity) throws IllegalAccessException, SQLException;

    public void update(Object entity) throws IllegalAccessException, SQLException;

    public <T> T findById(Long id, Class<T> aClass) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException;

}
