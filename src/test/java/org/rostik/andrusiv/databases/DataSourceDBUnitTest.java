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

import javax.sql.DataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.dbunit.Assertion.assertEqualsIgnoreCols;
import static org.rostik.andrusiv.databases.ConnectionSettings.*;
@Slf4j
@RunWith(JUnit4.class)
public class DataSourceDBUnitTest extends DataSourceBasedDBTestCase {

    private Connection connection;

    @Override
    protected DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL);
//        dataSource.setUser(USER);
//        dataSource.setPassword(PASSWORD);
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
    public void givenDataSet_whenSelect_thenFirstTitleIsGreyTShirt() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("select * from STUDENT where id = 101");
        assertTrue(rs.next());
        assertEquals("Homer", rs.getString("first_name"));
    }

    @Test
    public void testGivenDataSet_whenInsert_thenTableHasNewStudent() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("expected-student.xml")) {
            IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
            // given
            ITable expectedTable = expectedDataSet.getTable("STUDENT");

            connection.createStatement()
                    .executeUpdate(
                            "insert into student (first_name, last_name, dob, phone) values ('max','power','2000-12-12','987654321')");
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
    public void givenDataSet_whenDelete_thenItemIsDeleted() throws Exception {
        try (InputStream is = DataSourceDBUnitTest.class.getClassLoader()
                .getResourceAsStream("expected-delete.xml")) {
            // given
            ITable expectedTable = (new FlatXmlDataSetBuilder().build(is)).getTable("STUDENT");
            // when
            connection.createStatement().executeUpdate("delete from STUDENT where id = 102");
            // then
            IDataSet databaseDataSet = getConnection().createDataSet();
            ITable actualTable = databaseDataSet.getTable("STUDENT");
            assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id", "created_date", "updated_date"});
        }
    }

    @Test
    public void givenDataSet_whenUpdate_thenStudentHasNewName() throws Exception {
        try (InputStream is = DataSourceDBUnitTest.class.getClassLoader()
                .getResourceAsStream("student-exp-rename.xml")) {
            // given
            ITable expectedTable = (new FlatXmlDataSetBuilder().build(is)).getTable("STUDENT");
            // when
            connection.createStatement().executeUpdate("update STUDENT set first_name='New Homer' where id = 101");
            // then
            IDataSet databaseDataSet = getConnection().createDataSet();
            ITable actualTable = databaseDataSet.getTable("STUDENT");

            assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id", "created_date", "updated_date"});
        }
    }
}
