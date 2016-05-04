# aws-data-import-oracle

To build execute
./gradlew clean fatJar

To upload a file to your RDS Oracle instance:
 
java -jar aws-data-import-oracle-1.0-all.jar $ORACLE_JDBC_URL $ORACLE_USERNAME $ORACLE_PASSWORD /path/to/import/file

