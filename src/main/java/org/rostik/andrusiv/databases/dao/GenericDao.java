package org.rostik.andrusiv.databases.dao;

import java.util.List;

public interface GenericDao<T, ID> {

    List<T> findAll();

    T findById(ID id);

    boolean save(T entity);

    boolean update(ID id, T entity);

    void delete(ID id);

}
