package org.rostik.andrusiv.databases.dao.Impl;

import lombok.extern.slf4j.Slf4j;
import org.rostik.andrusiv.databases.dao.ExamResultsDao;
import org.rostik.andrusiv.databases.db.DbManager;
import org.rostik.andrusiv.databases.entity.ExamResult;
import org.rostik.andrusiv.databases.entity.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static java.sql.Connection.TRANSACTION_SERIALIZABLE;
@Slf4j
public class ExamResultsDaoImpl implements ExamResultsDao {


    private DbManager dbManager = DbManager.getInstance();

    private static final String EXCEPTION_MESSAGE = "SQL Exception: %s";

    private static final String SQL_FIND_ALL = "select * from exam_result";

    private static final String SQL_FIND_BY_ID = "select * from exam_result where id = ?";

    private static final String SQL_SAVE = "insert into exam_result (student_id, subject_id, mark) values (?,?,?)";

    private static final String SQL_UPDATE = "update exam_result set student_id = ?, subject_id = ?, mark = ? where id = ?";

    private static final String SQL_DELETE = "DELETE FROM exam_result WHERE id = ?";


    @Override
    public List<ExamResult> findAll() {
        List<ExamResult> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                ExamResult result = new ExamResult
                        (rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                list.add(result);
            }
        } catch (SQLException ex) {
            log.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public ExamResult findById(Integer id) {

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_ID)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            connection.commit();
            rs.next();
            return new ExamResult
                    (rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
        } catch (SQLException ex) {
            log.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return null;
    }

    @Override
    public boolean save(ExamResult result) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, result.getStudentId());
            preparedStatement.setInt(2, result.getSubjectId());
            preparedStatement.setInt(3, result.getMark());
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next())
                    result.setId(resultSet.getInt(1));
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
    public ExamResult update(Integer id, ExamResult result) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            preparedStatement.setInt(1, result.getStudentId());
            preparedStatement.setInt(2, result.getSubjectId());
            preparedStatement.setInt(3, result.getMark());
            preparedStatement.setInt(4, id);
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    result.setId(resultSet.getInt(1));
                }
                connection.commit();
                return result;
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
        return result;
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
