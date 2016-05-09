package uk.co.thinkdesigns.aws.oracle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class OracleFileUpload {

    private static final int BUFFER_SIZE = 32767;
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleFileUpload.class);

    private final Connection connection;
    private final int bufferSize;

    public OracleFileUpload(Connection connection, int bufferSize) {
        this.connection = connection;
        this.bufferSize = bufferSize;
    }

    public void upload(String filename, InputStream inputStream) throws SQLException, IOException {
        if ((filename == null) || filename.matches("^\\s*$")) {
            throw new IllegalArgumentException("A filename must be provided");
        }
        LOGGER.info("About to upload: {}", filename);
        CallableStatement stmt = connection.prepareCall("BEGIN file_handle_pkg.file_handle := utl_file.fopen('DATA_PUMP_DIR', :filename, 'wb', :line_size); END;");
        stmt.setString("filename", filename);
        stmt.setInt("line_size", bufferSize);
        stmt.execute();

        byte[] data = new byte[bufferSize];

        long totalBytesRead = 0;
        int bytesRead;
        stmt = connection.prepareCall("{CALL utl_file.put_raw(file_handle_pkg.file_handle, :data, true)}");

        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            stmt.setBytes("data", Arrays.copyOf(data, bytesRead));
            stmt.execute();
            data = new byte[bufferSize];
            LOGGER.debug("{} bytes uploaded", (totalBytesRead+=bytesRead));
        }

        stmt = connection.prepareCall("BEGIN utl_file.fclose(file_handle_pkg.file_handle); file_handle_pkg.file_handle := null; END;");
        stmt.execute();
        stmt.close();
        LOGGER.info("File: {} uploaded successfully", filename);
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

        if (args.length != 4) {
            showUsage();
            System.exit(-1);
        }

        Class.forName("oracle.jdbc.driver.OracleDriver");
        try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@" + args[0], args[1], args[2])) {
            File file = new File(args[3]);
            new OracleFileUpload(connection, BUFFER_SIZE).upload(file.getName(), new FileInputStream(file));
        }
    }

    public static void showUsage() {
        System.out.println("\nUsage:");
        System.out.println(OracleFileUpload.class.getName() + " <url> <username> <password> <filePath>");
        System.out.println("\nFor example:");
        System.out.println(OracleFileUpload.class.getName() + " localhost:1521:orcl hybris_import imp0rt /tmp/data.dmp");
    }
}
