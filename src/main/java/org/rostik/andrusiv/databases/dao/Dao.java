package org.rostik.andrusiv.databases.dao;

import java.util.List;

public interface Dao<T, ID> {

    List<T> findAll();

    T findById(ID id);

    boolean save(T entity);

    T update(ID id, T entity);

    void delete(ID id);

}
