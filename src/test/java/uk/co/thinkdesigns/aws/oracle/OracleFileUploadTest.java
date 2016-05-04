package uk.co.thinkdesigns.aws.oracle;

import org.junit.Test;
import uk.co.thinkdesigns.aws.oracle.OracleFileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class OracleFileUploadTest {

    private static final int BUFFER_SIZE = 5;

    private final Connection connection = mock(Connection.class);
    private final CallableStatement openFileStatement = mock(CallableStatement.class);
    private final CallableStatement writeFileStatement = mock(CallableStatement.class);
    private final CallableStatement closeFileStatement = mock(CallableStatement.class);

    private final OracleFileUpload underTest = new OracleFileUpload(connection, BUFFER_SIZE);

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownIfFilenameIsNull() throws IOException, SQLException {
        underTest.upload(null, mock(InputStream.class));

        verifyZeroInteractions(connection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownIfFilenameIsEmpty() throws IOException, SQLException {
        underTest.upload("", mock(InputStream.class));

        verifyZeroInteractions(connection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownIfFilenameIsWhitespace() throws IOException, SQLException {
        underTest.upload(" \t ", mock(InputStream.class));

        verifyZeroInteractions(connection);
    }

    @Test
    public void testName() throws Exception {
        when(connection.prepareCall(anyString()))
                .thenReturn(openFileStatement)
                .thenReturn(writeFileStatement)
                .thenReturn(closeFileStatement);

        underTest.upload("foo.txt", new ByteArrayInputStream("HelloWorld".getBytes()));

        verify(openFileStatement).setString("filename", "foo.txt");
        verify(openFileStatement).setInt("line_size", BUFFER_SIZE);
        verify(openFileStatement).execute();

        verify(writeFileStatement).setBytes("data", "Hello".getBytes());
        verify(writeFileStatement).setBytes("data", "World".getBytes());
        verify(writeFileStatement, times(2)).execute();

        verify(closeFileStatement).execute();
        verify(closeFileStatement).close();
    }
}