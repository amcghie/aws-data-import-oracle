package uk.co.thinkdesigns.aws.oracle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.thinkdesigns.aws.oracle.OracleFileUpload;

import java.io.ByteArrayInputStream;
import java.sql.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OracleFileUploadIntegrationTest {

    private static final String FILENAME = "helloWorld.txt";

    private static Connection connection;
    private final OracleFileUpload underTest = new OracleFileUpload(connection, 5);

    @BeforeClass
    public static void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String databaseUrl = System.getProperty("ORACLE_JDBC_URL", "localhost:1521/ORCL");
        connection = DriverManager.getConnection("jdbc:oracle:thin:@" + databaseUrl, "hybris_import", "imp0rt");
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        CallableStatement statement = connection.prepareCall("BEGIN utl_file.fremove('DATA_PUMP_DIR',:filename); END;");
        statement.setString("filename", FILENAME);
        statement.execute();
        connection.close();
    }

    @Test
    public void uploadFile() throws Exception {
        underTest.upload(FILENAME, new ByteArrayInputStream("Hello World".getBytes()));

        PreparedStatement statement = connection.prepareStatement("SELECT filesize FROM TABLE(RDSADMIN.RDS_FILE_UTIL.LISTDIR('DATA_PUMP_DIR')) WHERE filename = ?");
        statement.setString(1, FILENAME);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            assertThat(resultSet.getInt("filesize"), is(11));
        }
        resultSet.close();
        statement.close();
    }
}