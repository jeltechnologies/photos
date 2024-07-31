package com.jeltechnologies.photos.db;

import java.util.ArrayList;
import java.util.List;

public class DBSQL {
    public static final String CREATE_PHOTOS_TABLE = createPhotosTable();
    public static final String CREATE_FILES_TABLE = createFilesTable();
    public static final String INSERT_PHOTOS = insertPhotos();
    public static final String GET_PHOTOS = getPhotos();
    public static final String GET_PHOTOS_ATTRIBUTES = getPhotoAttributes();
    public static final String GET_PHOTOS_FILES_INNER_JOIN = getPhotosFileInnerJoin();
    public static final String UPDATE_PHOTOS = updatePhotos();

    public static final String CREATE_ALBUMS_TABLE = createAlbumsTable();
    public static final String GET_ALBUM = getAlbum();
    public static final String GET_ALBUMS = getAlbums();
    public static final String INSERT_ALBUM = insertAlbum();
    public static final String UPDATE_ALBUM = updateAlbum();
    public static final String UPDATE_ALBUM_RENAME = updateAlbumTitle();
    public static final String UPDATE_ALBUM_COVER = updateAlbumCover();
    public static final String CREATE_MEDIA_TYPES_TABLE = createMediaTypeTable();
    public static final String CREATE_SHARES_TABLE = createSharesTable();
    public static final String CREATE_PREFERENCES_TABLE = createPreferencesTable();
    
    public static final String CREATE_FRAME_LOGLINES_TABLE= createFrameLogLinesTable();

    private static String createPhotosTable() {
	StringBuilder bc = new StringBuilder();
	bc.append("CREATE TABLE IF NOT EXISTS Photos (");
	bc.append(" id TEXT PRIMARY KEY,");
	bc.append(" mediatype CHAR(1) NOT NULL, ");
	bc.append(" dateTaken TIMESTAMP NOT NULL,");
	bc.append(" thumbWidth INT,");
	bc.append(" thumbHeight INT,");
	bc.append(" orientation INT, ");
	bc.append(" latitude NUMERIC(20,16),");
	bc.append(" longitude NUMERIC(20,16),");
	bc.append(" duration INT,");
	bc.append(" source TEXT,");
	bc.append(" label TEXT,");
	bc.append(" hidden BOOLEAN NOT NULL DEFAULT False,");
	bc.append(" livephoto BOOLEAN NOT NULL DEFAULT False,");
	bc.append(" street TEXT,");
	bc.append(" housenr TEXT,");
	bc.append(" postalcode TEXT,");
	bc.append(" city TEXT,");
	bc.append(" countryCode TEXT,");
	bc.append(" distance NUMERIC(20,16),");
	bc.append(" CONSTRAINT media_type_fk FOREIGN KEY(mediatype) REFERENCES mediatypes(type)");
	bc.append(");");
	return bc.toString();
    }

    private static String createFilesTable() {
	StringBuilder f = new StringBuilder();
	f.append("CREATE TABLE IF NOT EXISTS Files(");
	f.append(" relativeFileName TEXT,");
	f.append(" photos_id TEXT,");
	f.append(" role TEXT NOT NULL DEFAULT 'photos-user',");
	f.append(" relativeFolderName TEXT,");
	f.append(" fileName TEXT,");
	f.append(" dateModified bigint,");
	f.append(" size BIGINT,");
	f.append(" inalbums BOOLEAN DEFAULT FALSE,");
	f.append(" CONSTRAINT files_pk PRIMARY KEY(relativeFileName),");
	f.append(" CONSTRAINT files_fk_photos FOREIGN KEY(photos_id) REFERENCES photos(id) ON DELETE CASCADE ON UPDATE CASCADE,");
	f.append(" CONSTRAINT files_fk_roles FOREIGN KEY(role) REFERENCES roles(role)");
	f.append(");");
	return f.toString();
    }

    protected static List<String> getIndexesSQL() {
	List<String> sqls = new ArrayList<>();
	sqls.add("CREATE INDEX IF NOT EXISTS photos_timeline ON photos USING brin (datetaken);");
	sqls.add("CREATE INDEX IF NOT EXISTS photos_map_latitude ON photos USING btree (latitude);");
	sqls.add("CREATE INDEX IF NOT EXISTS photos_map_longitude ON photos USING btree (longitude);");
	sqls.add("CREATE INDEX IF NOT EXISTS files_relativefoldername ON files USING btree (relativeFolderName);");
	sqls.add("CREATE INDEX IF NOT EXISTS files_inalbums ON files USING btree (inalbums);");
	return sqls;
    }

    protected static List<String> cleanDatabaseSQL() {
	List<String> sqls = new ArrayList<>();
//	sqls.add("VACUUM photos");
//	sqls.add("ANALYZE photos");
//	sqls.add("REINDEX TABLE photos");
	return sqls;
    }

    private static String insertPhotos() {
	StringBuilder bi = new StringBuilder();
	bi.append("INSERT INTO photos (");
	bi.append(" id,");
	bi.append(" mediatype,");
	bi.append(" dateTaken,");
	bi.append(" thumbWidth,");
	bi.append(" thumbHeight,");
	bi.append(" orientation,");
	bi.append(" latitude,");
	bi.append(" longitude,");
	bi.append(" duration,");
	bi.append(" source,");
	bi.append(" label,");
	bi.append(" hidden,");
	bi.append(" livephoto,");
	bi.append(" street,");
	bi.append(" postalcode,");
	bi.append(" housenr,");
	bi.append(" city,");
	bi.append(" countrycode,");
	bi.append(" distance");
	bi.append(") VALUES (");
	for (int i = 0; i < 19; i++) {
	    if (i > 0) {
		bi.append(",");
	    }
	    bi.append("?");
	}
	bi.append(")");
	return bi.toString();
    }

    private static String getPhotos() {
	StringBuilder se = new StringBuilder();
	se.append("SELECT ");
	se.append(getPhotoAttributes());
	se.append(" FROM photos p ");
	return se.toString();
    }

    private static String getPhotosFileInnerJoin() {
	StringBuilder se = new StringBuilder();
	se.append("SELECT ");
	se.append(GET_PHOTOS_ATTRIBUTES);
	se.append(",f.relativefilename");
	se.append(",f.relativefoldername");
	se.append(",f.filename");
	se.append(",f.role");
	se.append(" FROM photos p INNER JOIN files f ON p.id=photos_id");
	return se.toString();
    }
    
    private static String getPhotoAttributes() {
	StringBuilder se = new StringBuilder();
	se.append("p.id");
	se.append(",p.mediatype");
	se.append(",p.dateTaken");
	se.append(",p.thumbWidth");
	se.append(",p.thumbHeight");
	se.append(",p.orientation");
	se.append(",p.latitude");
	se.append(",p.longitude");
	se.append(",p.duration");
	se.append(",p.source");
	se.append(",p.label");
	se.append(",p.hidden");
	se.append(",p.livephoto");
	se.append(",p.street");
	se.append(",p.housenr");
	se.append(",p.postalcode");
	se.append(",p.city");
	se.append(",p.countrycode");
	se.append(",p.distance");
	return se.toString();
    }

    private static String updatePhotos() {
	StringBuilder b = new StringBuilder();
	b.append("UPDATE photos SET ");
	b.append(" mediatype = ?,");
	b.append(" dateTaken = ?,");
	b.append(" thumbWidth = ?,");
	b.append(" thumbHeight = ?,");
	b.append(" orientation = ?,");
	b.append(" latitude = ?,");
	b.append(" longitude = ?,");
	b.append(" duration = ?,");
	b.append(" source = ?,");
	b.append(" label = ?,");
	b.append(" hidden = ?,");
	b.append(" livephoto = ?,");
	b.append(" street = ?,");
	b.append(" housenr = ?,");
	b.append(" postalcode = ?,");
	b.append(" city = ?,");
	b.append(" countrycode = ?,");
	b.append(" distance = ?");
	b.append(" WHERE id = ?;");
	return b.toString();
    }

    public static String createAlbumsTable() {
	StringBuilder ab = new StringBuilder();
	ab.append("CREATE TABLE IF NOT EXISTS albums (");
	ab.append(" relativeFolderName TEXT PRIMARY KEY,");
	ab.append(" role TEXT NOT NULL DEFAULT 'photos-user',");
	ab.append(" name TEXT,");
	ab.append(" cover_photo_id TEXT,");
	ab.append(" CONSTRAINT privfk FOREIGN KEY(role) REFERENCES roles(role),");
	ab.append(" CONSTRAINT fk_cover_photo_id FOREIGN KEY(cover_photo_id) REFERENCES photos(id) ON DELETE CASCADE ON UPDATE CASCADE");
	ab.append(");");
	return ab.toString();
    }

    private static String getAlbum() {
	StringBuilder b = new StringBuilder();
	b.append(getAlbums());
	b.append(" WHERE a.relativeFolderName=?");
	return b.toString();
    }

    private static String getAlbums() {
	StringBuilder b = new StringBuilder();
	b.append("SELECT");
	b.append(" a.relativeFolderName, ");
	b.append(" a.role, ");
	b.append(" a.name,");
	b.append(" a.cover_photo_id,");
	b.append(" p.mediatype,");
	b.append(" p.thumbwidth,");
	b.append(" p.thumbheight,");
	b.append(" p.label");
	b.append(" FROM albums a LEFT OUTER JOIN photos p ON a.cover_photo_id = p.id");
	return b.toString();
    }

    private static String insertAlbum() {
	StringBuilder b = new StringBuilder();
	b.append("INSERT INTO albums (relativeFolderName,role,name,cover_photo_id) VALUES (?,?,?,?);");
	return b.toString();
    }

    private static String updateAlbumTitle() {
	StringBuilder b = new StringBuilder();
	b.append("UPDATE albums SET name = ? WHERE relativeFolderName = ?;");
	return b.toString();
    }

    private static String updateAlbum() {
	StringBuilder b = new StringBuilder();
	b.append("UPDATE albums");
	b.append(" SET");
	b.append(" role = ?,");
	b.append(" name = ?,");
	b.append(" cover_photo_id = ?");
	b.append(" WHERE relativeFolderName = ?;");
	return b.toString();
    }

    private static String updateAlbumCover() {
	StringBuilder b = new StringBuilder();
	b.append("UPDATE albums SET cover_photo_id=? WHERE relativeFolderName = ?;");
	return b.toString();
    }

    public static String createMediaTypeTable() {
	StringBuilder b = new StringBuilder();
	b.append("CREATE TABLE IF NOT EXISTS mediatypes (");
	b.append(" type CHAR(1) PRIMARY KEY,");
	b.append(" name TEXT");
	b.append(");");
	return b.toString();
    }

    public static String createRoles() {
	StringBuilder b = new StringBuilder();
	b.append("CREATE TABLE IF NOT EXISTS roles(");
	b.append(" role TEXT PRIMARY KEY");
	b.append(");");
	return b.toString();
    }

    private static String createSharesTable() {
	StringBuilder bc = new StringBuilder();
	bc.append("CREATE TABLE IF NOT EXISTS shares (");
	bc.append(" uuid TEXT PRIMARY KEY,");
	bc.append(" photos_id TEXT NOT NULL,");
	bc.append(" expirationdate TIMESTAMP NOT NULL,");
	bc.append(" creationdate TIMESTAMP NOT NULL, ");
	bc.append(" username TEXT NOT NULL,");
	bc.append(" CONSTRAINT shares_fk_photos FOREIGN KEY(photos_id) REFERENCES photos(id) ON DELETE CASCADE ON UPDATE CASCADE");
	bc.append(");");
	return bc.toString();
    }

    private static String createPreferencesTable() {
	StringBuilder b = new StringBuilder();
	b.append("CREATE TABLE IF NOT EXISTS preferences (");
	b.append(" username TEXT PRIMARY KEY,");
	b.append(" preferences TEXT");
	b.append(");");
	return b.toString();
    }
    
    private static String createFrameLogLinesTable() {
	StringBuilder b = new StringBuilder();
	b.append("CREATE TABLE IF NOT EXISTS frameloglines (");
	b.append(" timestamp TIMESTAMP NOT NULL,");
	b.append(" message TEXT NULL,");
	b.append(" id TEXT NOT NULL,");
	b.append(" username TEXT NOT NULL,");
	b.append(" session TEXT NOT NULL");
	b.append(");");
	return b.toString();
    }

}
