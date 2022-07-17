package org.rostik.andrusiv.databases;

import lombok.extern.slf4j.Slf4j;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.rostik.andrusiv.databases.dao.Impl.StudentDaoImpl;
import org.rostik.andrusiv.databases.dao.StudentDao;
import org.rostik.andrusiv.databases.entity.Student;

import javax.sql.DataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.dbunit.Assertion.assertEqualsIgnoreCols;
import static org.rostik.andrusiv.databases.ConnectionSettings.*;
@Slf4j
@RunWith(JUnit4.class)
public class DataSourceDBUnitTest extends DataSourceBasedDBTestCase {

    StudentDao studentDao = new StudentDaoImpl();

    private Connection connection;

    @Override
    protected DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL);
        return dataSource;
    }


    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(getClass().getClassLoader()
                .getResourceAsStream("data.xml"));
    }

    @Override
    protected DatabaseOperation getSetUpOperation() {
        return DatabaseOperation.REFRESH;
    }

    @Override
    protected DatabaseOperation getTearDownOperation() {
        return DatabaseOperation.DELETE_ALL;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        connection = getConnection().getConnection();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Test
    public void givenDataSet_whenSelect_thenFirstNameIsHomer() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("select * from STUDENT where id = 101");
        assertTrue(rs.next());
        assertEquals("Homer", rs.getString("first_name"));
    }

    @Test
    public void testGivenDataSet_whenInsert_thenTableHasNewStudent() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("expected-student-add.xml")) {
            IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
            // given
            ITable expectedTable = expectedDataSet.getTable("STUDENT");

            Student student = new Student("max", "power", LocalDate.parse("2000-12-12"), 987654321);
            studentDao.save(student);

            // when
            ITable actualData = getConnection()
                    .createQueryTable(
                            "result_name",
                            "SELECT * FROM STUDENT where first_name = 'max'");
            // then
            System.out.println(actualData);
            assertEqualsIgnoreCols(expectedTable, actualData, new String[]{"id", "created_date", "updated_date"});
        }
    }

    @Test
    public void givenDataSet_whenDelete_thenStudentIsDeleted() throws Exception {
        try (InputStream is = DataSourceDBUnitTest.class.getClassLoader()
                .getResourceAsStream("expected-student-delete.xml")) {
            // given
            ITable expectedStudentTable = (new FlatXmlDataSetBuilder().build(is)).getTable("STUDENT");
            // when
            studentDao.delete(102);
            // then
            IDataSet databaseStudentDataSet = getConnection().createDataSet();
            ITable actualStudentTable = databaseStudentDataSet.getTable("STUDENT");
            assertEqualsIgnoreCols(expectedStudentTable, actualStudentTable, new String[]{"id", "created_date", "updated_date"});
        }
    }
    @Test
    public void givenDataSet_whenDeleteStudent_thenResultIsDeleted() throws Exception {
        try (InputStream is = DataSourceDBUnitTest.class.getClassLoader()
                .getResourceAsStream("expected-student-delete.xml")) {
            // given
            ITable expectedResultTable = (new FlatXmlDataSetBuilder().build(is)).getTable("EXAM_RESULT");
            // when
            studentDao.delete(102);
            // then
            IDataSet databaseResultDataSet = getConnection().createDataSet();
            ITable actualResultTable = databaseResultDataSet.getTable("EXAM_RESULT");
            assertEqualsIgnoreCols(expectedResultTable, actualResultTable, new String[]{"id"});
        }
    }

    @Test
    public void givenDataSet_whenUpdate_thenStudentHasNewName() throws Exception {
        try (InputStream is = DataSourceDBUnitTest.class.getClassLoader()
                .getResourceAsStream("expected-student-rename.xml")) {
            // given
            ITable expectedTable = (new FlatXmlDataSetBuilder().build(is)).getTable("STUDENT");
            // when
            studentDao.update(101, new Student("New Homer", "Simpson", LocalDate.parse("1956-12-12"), 123456789));
            // then
            IDataSet databaseDataSet = getConnection().createDataSet();
            ITable actualTable = databaseDataSet.getTable("STUDENT");
            assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id", "created_date", "updated_date"});
        }
    }
}
