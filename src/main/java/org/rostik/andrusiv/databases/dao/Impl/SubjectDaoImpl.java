package org.rostik.andrusiv.databases.dao.Impl;

import lombok.extern.slf4j.Slf4j;
import org.rostik.andrusiv.databases.dao.SubjectDao;
import org.rostik.andrusiv.databases.db.DbManager;
import org.rostik.andrusiv.databases.entity.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

@Slf4j
public class SubjectDaoImpl implements SubjectDao {

    private DbManager dbManager = DbManager.getInstance();

    private static final String EXCEPTION_MESSAGE = "SQL Exception: %s";

    private static final String SQL_FIND_ALL = "select * from subject";

    private static final String SQL_FIND_BY_ID = "select * from subject where id = ?";

    private static final String SQL_SAVE = "insert into subject (subject_name, tutor) values (?,?)";

    private static final String SQL_UPDATE = "update subject set subject_name = ?, tutor = ? where id = ?";

    private static final String SQL_DELETE = "DELETE FROM subject WHERE id = ?";


    @Override
    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                Subject subject = new Subject
                        (rs.getInt(1), rs.getString(2), rs.getString(3));
                list.add(subject);
            }
        } catch (SQLException ex) {
            log.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Subject findById(Integer id) {

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_ID)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            connection.commit();
            rs.next();
            return new Subject
                    (rs.getInt(1), rs.getString(2), rs.getString(3));
        } catch (SQLException ex) {
            log.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return null;
    }

    @Override
    public boolean save(Subject subject) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, subject.getSubjectName());
            preparedStatement.setString(2, subject.getTutor());
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next())
                    subject.setId(resultSet.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        } finally {
            close(resultSet);
        }
        return false;
    }

    @Override
    public Subject update(Integer id, Subject subject) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            preparedStatement.setString(1, subject.getSubjectName());
            preparedStatement.setString(2, subject.getTutor());
            preparedStatement.setInt(3, id);
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    subject.setId(resultSet.getInt(1));
                }
                connection.commit();
                return subject;
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
        return subject;
    }

    @Override
    public void delete(Integer id) {
        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_DELETE);
        ) {
            pst.setInt(1, id);
            pst.execute();
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }

    private static void close(AutoCloseable ac) {
        try {
            if (ac != null) {
                ac.close();
            }
        } catch (Exception e) {
            log.info(String.format("exception: %s", e.getMessage()));
        }
    }
}
