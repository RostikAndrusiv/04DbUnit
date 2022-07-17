package org.rostik.andrusiv.databases.dao.Impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rostik.andrusiv.databases.dao.StudentDao;
import org.rostik.andrusiv.databases.db.DbManager;
import org.rostik.andrusiv.databases.entity.ExamResult;
import org.rostik.andrusiv.databases.entity.Student;
import org.rostik.andrusiv.databases.entity.StudentWithMarks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

@Slf4j
@Data
@NoArgsConstructor
public class StudentDaoImpl implements StudentDao {

    private static final String EXCEPTION_MESSAGE = "SQL Exception: %s";

    private static final String SQL_FIND_ALL = "select * from student";

    private static final String SQL_FIND_BY_ID = "select * from student where id = ?";

    private static final String SQL_FIND_BY_NAME_LASTNAME_EXACT = "select * from student where first_name  = ? and last_name = ?";

    private static final String SQL_FIND_BY_LASTNAME_PARTIAL = "select * from student where last_name LIKE ?";

    private static final String SQL_FIND_BY_PHONE_PARTIAL = "select * from student where cast(phone as text) like ? || '%'";

    private static final String SQL_FIND_BY_SURNAME_WITH_MARKS_PARTIAL = "select * from student join exam_results on student.id = exam_results.id where last_name like ?";

    private static final String SQL_SAVE = "insert into student (first_name, last_name, dob, phone) values (?,?,?,?)";

    private static final String SQL_UPDATE = "update student set first_name = ?, last_name = ?, dob = ?, phone = ? where id = ?";

    private static final String SQL_DELETE = "DELETE FROM student WHERE id = ?";

    DbManager dbManager = DbManager.getInstance();

    private static Logger logger = LoggerFactory.getLogger(StudentDaoImpl.class.getName());

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                Student student = new Student
                        (rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getDate(4).toLocalDate(), rs.getInt(5),
                                rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
                list.add(student);
            }
        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Student findById(Integer id) {

        try (Connection connection = dbManager.getConnection();
            PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_ID)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            connection.commit();
            rs.next();
            return new Student
                    (rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getDate(4).toLocalDate(), rs.getInt(5),
                            rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return null;
    }

    @Override
    public boolean save(Student student) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, student.getName());
            preparedStatement.setString(2, student.getSurName());
            preparedStatement.setDate(3, Date.valueOf(student.getDob()));
            preparedStatement.setInt(4, student.getPhoneNumber());
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next())
                    student.setId(resultSet.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
        } finally {
            close(resultSet);
        }
        return false;
    }

    @Override
    public Student update(Integer id, Student student) {
        ResultSet resultSet = null;
        try (Connection connection = dbManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            preparedStatement.setString(1, student.getName());
            preparedStatement.setString(2, student.getSurName());
            preparedStatement.setDate(3, Date.valueOf(student.getDob()));
            preparedStatement.setInt(4, student.getPhoneNumber());
            preparedStatement.setInt(5, id);
            if (preparedStatement.executeUpdate() > 0) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    student.setId(resultSet.getInt(1));
                }
                connection.commit();
                return student;
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
        return student;
    }

    @Override
    public void delete(Integer id) {
        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_DELETE);
        ) {
            pst.setInt(1, id);
            pst.execute();
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }


    public List<Student> findStudentByNameExact(String firstName, String lastName) {
        List<Student> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_NAME_LASTNAME_EXACT)) {

            pst.setString(1, firstName);
            pst.setString(2, lastName);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Student student = new Student
                        (rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getDate(4).toLocalDate(), rs.getInt(5),
                                rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
                list.add(student);
            }

        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return list;
    }

    public List<Student> findStudentByLastNamePartial(String lastName) {
        List<Student> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_LASTNAME_PARTIAL)) {

            pst.setString(1, "%" + lastName + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Student student = new Student
                        (rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getDate(4).toLocalDate(), rs.getInt(5),
                                rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
                list.add(student);
            }

        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return list;
    }

    public List<Student> findStudentByPhonePartial(int phone) {
        List<Student> list = new ArrayList<>();

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_PHONE_PARTIAL)) {

            pst.setInt(1, phone);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Student student = new Student
                        (rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getDate(4).toLocalDate(), rs.getInt(5),
                                rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
                list.add(student);
            }

        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return list;
    }

    public List<StudentWithMarks> findStudentWithMarksByLastNamePartial(String lastName) {
        Map<Integer, StudentWithMarks> map = new HashMap<>();

        try (Connection connection = dbManager.getConnection();
             PreparedStatement pst = connection.prepareStatement(SQL_FIND_BY_SURNAME_WITH_MARKS_PARTIAL)) {

            pst.setString(1, "%" + lastName + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                if (!map.containsKey(rs.getInt(1))) {
                    StudentWithMarks student = new StudentWithMarks
                            (rs.getInt(1), rs.getString(2), rs.getString(3),
                                    rs.getDate(4).toLocalDate(), rs.getInt(5),
                                    rs.getTimestamp(6).toLocalDateTime(), rs.getTimestamp(7).toLocalDateTime());
                    student.getExamResultList().add(new ExamResult(rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11)));
                    map.put(student.getId(), student);
                } else {
                    List<ExamResult> examResults = map.get(rs.getInt(1)).getExamResultList();
                    examResults.add(new ExamResult(rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11)));
                }
            }

        } catch (SQLException ex) {
            logger.info(String.format(EXCEPTION_MESSAGE, ex.getMessage()));
        }
        return new ArrayList<>(map.values());
    }

    private static void close(AutoCloseable ac) {
        try {
            if (ac != null) {
                ac.close();
            }
        } catch (Exception e) {
            logger.info(String.format("exception: %s", e.getMessage()));
        }
    }


}
