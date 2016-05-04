CREATE USER hybris_import
  IDENTIFIED BY imp0rt
  DEFAULT TABLESPACE users;

GRANT CREATE SESSION TO hybris_import;
GRANT WRITE ON DIRECTORY data_pump_dir TO hybris_import;
GRANT READ ON DIRECTORY data_pump_dir TO hybris_import;
GRANT EXECUTE ON RDSADMIN.RDS_FILE_UTIL TO hybris_import

CREATE PACKAGE hybris_import.file_handle_pkg AS
  file_handle utl_file.file_type;
END;
/

GRANT EXECUTE ON hybris_import.file_handle_pkg TO hybris_import;

SELECT * FROM TABLE(RDSADMIN.RDS_FILE_UTIL.LISTDIR('DATA_PUMP_DIR')) order by mtime;