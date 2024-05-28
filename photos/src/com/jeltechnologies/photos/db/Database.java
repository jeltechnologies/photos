package com.jeltechnologies.photos.db;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Address;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datamodel.Distance;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.Preferences;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Query.InAlbum;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.MediaFile;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.picures.share.SharedFile;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

public class Database implements QuerySupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private Connection connection;
    private final int MAX_RETRIES = 10;
    private final int SLEEP_BEFORE_RETRY_MILLISECONDS = 1000;
    private final static Environment env = Environment.INSTANCE;
    private final int transactionIsolation;
    private final static String DATABASE_JDNI = Environment.INSTANCE.getConfig().getDatabaseJndi();

    private Map<String, PreparedStatement> usedPreparedStatements = new HashMap<String, PreparedStatement>();

    public Database() {
	transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
	init();
    }

    public Database(int transactionIsolation) {
	this.transactionIsolation = transactionIsolation;
	init();
    }

    private void init() {
	try {
	    InitialContext cxt = new InitialContext();
	    DataSource ds = (DataSource) cxt.lookup(DATABASE_JDNI);
	    connection = ds.getConnection();
	    connection.setTransactionIsolation(transactionIsolation);
	    connection.setAutoCommit(false);
	} catch (Exception e) {
	    throw new IllegalStateException("Cannot connect to database", e);
	}
    }

    public void close() {
	for (String sql : usedPreparedStatements.keySet()) {
	    DBUtils.close(usedPreparedStatements.get(sql));
	}
	commit();
	if (connection != null) {
	    try {
		connection.close();
	    } catch (SQLException e) {
		LOGGER.warn("Error while closing database connection", e);
	    }
	}
    }

    private PreparedStatement getStatement(String sql) throws SQLException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("getStatement sql: " + sql);
	}
	PreparedStatement statement = this.usedPreparedStatements.get(sql);
	if (statement == null) {
	    statement = connection.prepareStatement(sql);
	    this.usedPreparedStatements.put(sql, statement);
	}
	statement.clearParameters();
	return statement;
    }

    public boolean isClosed() throws SQLException {
	return connection.isClosed();
    }

    public void commit() {
	try {
	    connection.commit();
	} catch (SQLException e) {
	    LOGGER.warn("Cannot commit transaction");
	}
    }

    public void rollback() {
	try {
	    connection.commit();
	} catch (SQLException e) {
	    LOGGER.warn("Cannot rollback transaction");
	}
    }

    public void createDatabaseTablesNotExists() throws SQLException {
	preparePrivileges();
	executeSQL(DBSQL.CREATE_MEDIA_TYPES_TABLE);
	insertMediaTypeIfNotExists("p", "PHOTO");
	insertMediaTypeIfNotExists("v", "VIDEO");
	executeSQL(DBSQL.CREATE_PHOTOS_TABLE);
	executeSQL(DBSQL.CREATE_ALBUMS_TABLE);
	executeSQL(DBSQL.CREATE_FILES_TABLE);
	executeSQL(DBSQL.CREATE_PREFERENCES_TABLE);
	executeSQL(DBSQL.CREATE_SHARES_TABLE);
	for (String sql : DBSQL.getIndexesSQL()) {
	    executeSQL(sql);
	}
	connection.commit();
	connection.setAutoCommit(true);

	for (String sql : DBSQL.cleanDatabaseSQL()) {
	    executeSQL(sql);
	}
	connection.setAutoCommit(false);
    }

    private boolean doesTableExist(String tableName) throws SQLException {
	String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?;";
	PreparedStatement st = getStatement(sql);
	st.setString(1, tableName);
	ResultSet r = null;
	try {
	    r = st.executeQuery();
	    return r.next();
	} finally {
	    if (r != null) {
		r.close();
	    }
	}
    }

    private void preparePrivileges() throws SQLException {
	boolean privilegesExist = doesTableExist("roles");
	if (!privilegesExist) {
	    executeSQL(DBSQL.createRoles());
	}
	insertRole(RoleModel.ROLE_USER);
	insertRole(RoleModel.ROLE_ADMIN);
	commit();
    }

    private void insertRole(Role role) throws SQLException {
	String sqlGet = "SELECT role FROM roles WHERE role=?";
	PreparedStatement ps = getStatement(sqlGet);
	ResultSet r = null;
	try {
	    ps.setString(1, role.name());
	    r = ps.executeQuery();
	    boolean roleExists = r.next();
	    if (!roleExists) {
		String sqlRole = "INSERT INTO roles (role) VALUES (?)";
		PreparedStatement stRole = getStatement(sqlRole);
		stRole.setString(1, role.name());
		stRole.executeUpdate();
	    }
	} finally {
	    DBUtils.close(r);
	}
    }

    private void insertMediaTypeIfNotExists(String type, String name) throws SQLException {
	ResultSet rs = null;
	try {
	    PreparedStatement ps = getStatement("SELECT name FROM mediatypes WHERE type = ?");
	    ps.setString(1, type);
	    rs = ps.executeQuery();
	    boolean exists = rs.next();
	    if (!exists) {
		PreparedStatement is = getStatement("INSERT INTO mediatypes (type, name) VALUES (? , ?);");
		is.setString(1, type);
		is.setString(2, name);
		is.executeUpdate();
	    }
	} finally {
	    DBUtils.close(rs);
	}
    }

    private void executeSQL(String sql) throws SQLException {
	PreparedStatement statement = getStatement(sql);
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info(sql);
	}
	statement.execute();
    }

    private void restrictToAdmin(User user) {
	if (!user.isAdmin()) {
	    throw new IllegalArgumentException("User " + user + " is not administrator");
	}
    }

    public List<Photo> getAllPhotosWithFile() throws SQLException {
	List<Photo> photos = new ArrayList<Photo>();
	ResultSet rs = null;
	try {
	    PreparedStatement statement = getStatement(DBSQL.GET_PHOTOS_FILES_INNER_JOIN);
	    rs = statement.executeQuery();
	    while (rs.next()) {
		Photo photo = parsePhotoFilesSelect(rs, false);
		photos.add(photo);
	    }
	    return photos;
	} finally {
	    DBUtils.close(rs);
	}
    }

    public Photo getPhotoByFileName(User user, String relativeFileName) throws SQLException {
	ResultSet rs = null;
	try {
	    StringBuilder roles = getRestrictionsForUser(user, "f");
	    StringBuilder sql = new StringBuilder(DBSQL.GET_PHOTOS_FILES_INNER_JOIN);
	    sql.append(" WHERE f.relativeFileName=?");
	    if (roles.length() > 0) {
		sql.append(" AND ").append(roles);
	    }
	    PreparedStatement ps = getStatement(sql.toString());
	    ps.setString(1, relativeFileName);
	    rs = ps.executeQuery();
	    Photo photo = null;
	    if (rs.next()) {
		photo = parsePhotoFilesSelect(rs, false);
	    }
	    return photo;
	} finally {
	    DBUtils.close(rs);
	}
    }

    public boolean hasPhotoWithId(String id) throws SQLException {
	PreparedStatement st = getStatement("SELECT id FROM photos WHERE id=?");
	st.clearParameters();
	st.setString(1, id);
	ResultSet rs = null;
	try {
	    rs = st.executeQuery();
	    boolean result = rs.next();
	    return result;
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
    }

    public Photo getFirstPhotoById(User user, String id) throws SQLException {
	List<Photo> photos = getPhotosById(user, id);
	if (photos.isEmpty()) {
	    return null;
	} else {
	    return photos.get(0);
	}
    }

    public List<Photo> getPhotosById(User user, String id) throws SQLException {
	List<Photo> photos = new ArrayList<>();
	ResultSet rs = null;
	try {
	    StringBuilder roles = getRestrictionsForUser(user, "f");
	    StringBuilder sql = new StringBuilder(DBSQL.GET_PHOTOS_FILES_INNER_JOIN);
	    sql.append(" WHERE p.id=?");
	    if (roles.length() > 0) {
		sql.append(" AND ").append(roles);
	    }
	    PreparedStatement ps = getStatement(sql.toString());
	    ps.setString(1, id);
	    rs = ps.executeQuery();
	    while (rs.next()) {
		Photo photo = parsePhotoFilesSelect(rs, false);
		photos.add(photo);
	    }
	    return photos;
	} finally {
	    DBUtils.close(rs);
	}
    }

    public Photo getPhotoById(String id) throws SQLException {
	ResultSet rs = null;
	try {
	    StringBuilder s = new StringBuilder(DBSQL.GET_PHOTOS);
	    s.append(" WHERE p.id=?");
	    PreparedStatement ps = getStatement(s.toString());
	    ps.setString(1, id);
	    rs = ps.executeQuery();
	    Photo photo;
	    if (rs.next()) {
		photo = parsePhotoFull(rs);
	    } else {
		photo = null;
	    }
	    return photo;
	} finally {
	    DBUtils.close(rs);
	}
    }
    
    public List<Photo> getAllPhotos() throws SQLException {
	ResultSet rs = null;
	List<Photo> photos = new ArrayList<Photo>();
	try {
	    StringBuilder s = new StringBuilder(DBSQL.GET_PHOTOS);
	    PreparedStatement ps = getStatement(s.toString());
	    rs = ps.executeQuery();
	    Photo photo;
	    while (rs.next()) {
		photo = parsePhotoFull(rs);
		photos.add(photo);
	    } 
	} finally {
	    DBUtils.close(rs);
	}
	return photos;
    }

    public List<Photo> getPhotosByLocalFileName(User user, String localFileName, boolean caseSensitive) throws SQLException {
	ResultSet rs = null;
	try {
	    List<Photo> photos = new ArrayList<Photo>();
	    StringBuilder sql = new StringBuilder(DBSQL.GET_PHOTOS_FILES_INNER_JOIN);

	    if (caseSensitive) {
		sql.append(" WHERE f.filename=?");
	    } else {
		sql.append(" WHERE f.filename ILIKE ?");
	    }
	    StringBuilder userRestrictions = getRestrictionsForUser(user, "f");
	    if (userRestrictions.length() > 0) {
		sql.append(" AND ").append(userRestrictions);
	    }
	    PreparedStatement ps = getStatement(sql.toString());
	    ps.setString(1, localFileName);
	    rs = ps.executeQuery();
	    Photo photo = null;
	    while (rs.next()) {
		photo = parsePhotoFull(rs);
		photos.add(photo);
	    }
	    return photos;
	} finally {
	    DBUtils.close(rs);
	}
    }

    public int deletePhoto(User user, Photo photo) throws SQLException {
	restrictToAdmin(user);
	PreparedStatement ps = null;
	ps = getStatement("DELETE FROM photos WHERE id=?");
	ps.setString(1, photo.getId());
	int rows = ps.executeUpdate();
	return rows;
    }

    private StringBuilder getRestrictionsForUser(User user, String tableAlias) {
	StringBuilder wb = new StringBuilder();
	List<Role> roles = user.roles();
	if (roles != null && !roles.isEmpty() && !user.isAdmin()) {
	    int roleCount = 0;
	    wb.append("(");
	    for (Role role : user.roles()) {
		if (roleCount > 0) {
		    wb.append("OR ");
		}
		wb.append(tableAlias).append(".role='").append(role.name()).append("' ");
		roleCount++;
	    }
	    wb.append(") ");
	}
	return wb;
    }

    public List<Photo> query(Query query) throws SQLException {
	User user = query.getUser();
	if (user == null) {
	    throw new IllegalArgumentException("No user defined for query: " + query);
	}

	List<Photo> photos = new ArrayList<Photo>();
	Set<String> photosInAlbumsThatAreUnique = null;

	int paramId = -1;
	int paramRoles = -1;
	int paramMediaType = -1;
	int paramMinDuration = -1;
	int paramMaxDuration = -1;
	int paramRelativeFolderName = -1;
	int paramInAlbumns = -1;
	int paramPeriodFrom = -1;
	int paramPeriodTo = -1;
	int paramMapNorthEastLat = -1;
	int paramMapNorthEastLng = -1;
	int paramMapSouthWestLat = -1;
	int paramMapSouthWestLng = -1;
	int paramIncludeHidden = -1;

	StringBuilder sql = new StringBuilder();
	if (query.isOnlyReturnChecksums()) {
	    sql.append("SELECT p.id, p.mediatype FROM photos p INNER JOIN files f ON p.id=f.photos_id");
	} else {
	    sql.append(DBSQL.GET_PHOTOS_FILES_INNER_JOIN);
	}

	List<String> whereParts = new ArrayList<String>();
	int paraPos = 0;

	if (query.getId() != null) {
	    paraPos++;
	    paramId = paraPos;
	    whereParts.add("p.id=?");
	}

	boolean userHasRoles = user.roles() != null && !user.roles().isEmpty() && !user.isAdmin();
	if (userHasRoles) {
	    paramRoles = paraPos + 1;
	    StringBuilder wb = new StringBuilder("(");
	    for (int i = 0; i < user.roles().size(); i++) {
		if (i > 0) {
		    wb.append(" OR ");
		}
		wb.append("f.role=?");
		paraPos++;
	    }
	    wb.append(")");
	    whereParts.add(wb.toString());
	}

	if (query.getMediaType() != null && query.getMediaType() != MediaType.ALL) {
	    paraPos++;
	    paramMediaType = paraPos;
	    whereParts.add("p.mediatype=?");
	}

	if (query.getRelativeFolderName() != null) {
	    paraPos++;
	    paramRelativeFolderName = paraPos;
	    if (query.isIncludeSubFolders()) {
		whereParts.add("f.relativeFolderName LIKE ?");
	    } else {
		whereParts.add("f.relativeFolderName=?");
	    }
	}

	switch (query.getInAlbums()) {
	    case ALL_PHOTOS:
		break;
	    case IN_ALBUM_NO_DUPLICATES:
		paraPos++;
		paramInAlbumns = paraPos;
		whereParts.add("f.inalbums=?");
		photosInAlbumsThatAreUnique = new HashSet<String>();
		break;
	    case IN_ALBUM_WITH_DUPLICATES:
		paraPos++;
		paramInAlbumns = paraPos;
		whereParts.add("f.inalbums=?");
		break;
	    default:
		break;
	}

	TimePeriod period = query.getTimePeriod();
	if (period != null) {
	    LocalDate from = period.getFrom();
	    LocalDate to = period.getTo();
	    if (from != null && to != null) {
		paraPos++;
		paramPeriodFrom = paraPos;
		paraPos++;
		paramPeriodTo = paraPos;
		whereParts.add("p.dateTaken BETWEEN ? AND ?");
	    } else {
		if (from != null) {
		    paraPos++;
		    paramPeriodFrom = paraPos;
		    whereParts.add("p.dateTaken >= ?");
		} else {
		    paraPos++;
		    paramPeriodTo = paraPos;
		    whereParts.add("p.dateTaken <= ?");
		}
	    }
	}

	if (query.getMinimumDuration() > 0) {
	    paraPos++;
	    paramMinDuration = paraPos;
	    whereParts.add("p.duration = 0 OR p.duration >= ?");
	}

	if (query.getMaximumDuration() > 0) {
	    paraPos++;
	    paramMaxDuration = paraPos;
	    whereParts.add("p.duration <= ?");
	}

	if (query.getMapBounds() != null) {
	    paraPos++;
	    paramMapNorthEastLat = paraPos;
	    paraPos++;
	    paramMapSouthWestLat = paraPos;
	    paraPos++;
	    paramMapNorthEastLng = paraPos;
	    paraPos++;
	    paramMapSouthWestLng = paraPos;
	    whereParts.add("(p.latitude < ? AND p.latitude > ?) AND (p.longitude < ? AND p.longitude > ?)");
	}

	if (!query.isIncludeHidden()) {
	    paraPos++;
	    paramIncludeHidden = paraPos;
	    whereParts.add("p.hidden = ?");
	}

	for (int part = 0; part < whereParts.size(); part++) {
	    String wherePart = whereParts.get(part);
	    if (part == 0) {
		sql.append(" WHERE ");
	    } else {
		sql.append(" AND ");
	    }
	    sql.append("(").append(wherePart).append(")");
	}

	OrderBy orderBy = query.getOrderBy();
	if (orderBy != null) {
	    String orderClause;
	    switch (orderBy) {
		case DATE_TAKEN_NEWEST: {
		    orderClause = "p.dateTaken DESC";
		    break;
		}
		case DATE_TAKEN_OLDEST: {
		    orderClause = "p.dateTaken ASC";
		    break;
		}
		case FILENAME: {
		    orderClause = "f.relativeFileName";
		    break;
		}
		default: {
		    orderClause = null;
		}
	    }
	    sql.append(" ORDER BY ").append(orderClause);
	}

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Query : " + query.toString());
	    LOGGER.debug("SQL   : " + sql.toString());
	}

	ResultSet rs = null;
	try {
	    PreparedStatement ps = getStatement(sql.toString());

	    if (paramId > 0) {
		ps.setString(paramId, query.getId());
	    }

	    if (paramRoles > 0) {
		int roleIndex = 0;
		for (Role role : user.roles()) {
		    ps.setString(paramRoles + roleIndex, role.name());
		    roleIndex++;
		}
	    }

	    if (paramMediaType > 0) {
		MediaType type = query.getMediaType();
		String code;
		switch (type) {
		    case VIDEO: {
			code = "v";
			break;
		    }
		    case PHOTO: {
			code = "p";
			break;
		    }
		    default: {
			throw new IllegalArgumentException("Unsupported MediaType: " + type);
		    }
		}
		ps.setString(paramMediaType, code);
	    }

	    if (paramRelativeFolderName > 0) {
		String searchFor = query.getRelativeFolderName();
		if (query.isIncludeSubFolders()) {
		    searchFor = searchFor + "%";
		}
		ps.setString(paramRelativeFolderName, searchFor);
	    }

	    if (paramInAlbumns > 0) {
		ps.setBoolean(paramInAlbumns, true);
	    }

	    if (paramPeriodFrom > 0) {
		LocalDate date = query.getTimePeriod().getFrom();
		LocalDateTime startOfDay = date.atStartOfDay();
		ps.setObject(paramPeriodFrom, startOfDay);
	    }

	    if (paramPeriodTo > 0) {
		LocalDate date = query.getTimePeriod().getTo();
		LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
		ps.setObject(paramPeriodTo, endOfDay);
	    }

	    if (paramMinDuration > 0) {
		ps.setInt(paramMinDuration, query.getMinimumDuration());
	    }

	    if (paramMaxDuration > 0) {
		ps.setInt(paramMaxDuration, query.getMaximumDuration());
	    }

	    if (paramMapNorthEastLat > 0) {
		MapBounds bounds = query.getMapBounds();
		// whereParts.add("(latitude > ? AND latitude < ?) AND (longitude > ? AND longitide < ?");
		DBUtils.setBigDecimal(ps, paramMapNorthEastLat, bounds.getNorthEast().getLatitude());
		DBUtils.setBigDecimal(ps, paramMapNorthEastLng, bounds.getNorthEast().getLongitude());
		DBUtils.setBigDecimal(ps, paramMapSouthWestLat, bounds.getSouthWest().getLatitude());
		DBUtils.setBigDecimal(ps, paramMapSouthWestLng, bounds.getSouthWest().getLongitude());
	    }

	    if (paramIncludeHidden > 0) {
		ps.setBoolean(paramIncludeHidden, query.isIncludeHidden());
	    }

	    rs = ps.executeQuery();
	    boolean noDuplicates = query.getInAlbums() == InAlbum.IN_ALBUM_NO_DUPLICATES;
	    while (rs.next()) {
		Photo photo = parsePhotoFilesSelect(rs, query.isOnlyReturnChecksums());
		boolean add;
		if (!noDuplicates) {
		    add = true;
		} else {
		    if (!photosInAlbumsThatAreUnique.contains(photo.getId())) {
			add = true;
			photosInAlbumsThatAreUnique.add(photo.getId());
		    } else {
			add = false;
		    }
		}
		if (add) {
		    photos.add(photo);
		}
	    }
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Result: " + photos.size() + " photos");
	}
	return photos;
    }

    private Photo parsePhotoFilesSelect(ResultSet rs, boolean onlyReturnChecksum) throws SQLException {
	Photo p;
	if (onlyReturnChecksum) {
	    String id = rs.getString(1);
	    MediaType mediaType = null;
	    String type = rs.getString(2);
	    if (type.equalsIgnoreCase("p")) {
		mediaType = MediaType.PHOTO;
	    }
	    if (type.equalsIgnoreCase("v")) {
		mediaType = MediaType.VIDEO;
	    }
	    p = new Photo(id, mediaType);
	} else {
	    p = parsePhotoFull(rs);
	    int column = 20;

//	se.append(",f.relativefilename");
	    p.setRelativeFileName(rs.getString(column));
	    column++;
//	se.append(",f.relativefoldername");
	    p.setRelativeFolderName(rs.getString(column));
	    column++;
//	se.append(",f.filename");
	    p.setFileName(rs.getString(column));
	    column++;
//	se.append(",f.role");
	    // p.set

	}

	return p;
    }

    private Photo parsePhotoFull(ResultSet rs) throws SQLException {
	int column = 0;

//	1 se.append(" p.id,");
	column++;
	String id = rs.getString(column);

//	2 se.append(" p.mediatype,");
	column++;
	MediaType mediaType = null;
	String type = rs.getString(column);
	if (type.equalsIgnoreCase("p")) {
	    mediaType = MediaType.PHOTO;
	}
	if (type.equalsIgnoreCase("v")) {
	    mediaType = MediaType.VIDEO;
	}

	Photo p = new Photo(id, mediaType);

//	4 se.append(" p.dateTaken,");
	column++;
	p.setDateTaken(DBUtils.getDateTime(rs, column));

//	5 se.append(" p.thumbWidth,");
	column++;
	p.setThumbWidth(rs.getInt(column));

//	6 se.append(" p.thumbHeight,");
	column++;
	p.setThumbHeight(rs.getInt(column));

//	7 se.append(" p.orientation, ");
	column++;
	p.setOrientation(rs.getInt(column));

//	8 se.append(" p.latitude,");
	column++;
	BigDecimal latutide = DBUtils.getBigDecimal(rs, column);

//	9 se.append(" p.longitude,");
	column++;
	BigDecimal longitude = DBUtils.getBigDecimal(rs, column);
	// LOGGER.info(latutide + ", " + longitude);
	if (latutide != null && longitude != null) {
	    p.setCoordinates(new Coordinates(latutide.doubleValue(), longitude.doubleValue()));
	}

//	10 se.append(" p.duration,");
	column++;
	p.setDuration(rs.getInt(column));

//	11 se.append(" p.source,");
	column++;
	p.setSource(rs.getString(column));

//	12 se.append(" p.label,");
	column++;
	p.setLabel(rs.getString(column));

//	13 se.append(" p.hidden ");
	column++;
	p.setHidden(rs.getBoolean(column));

	column++;
	p.setLivePhoto(rs.getBoolean(column));

	column++;
	String street = rs.getString(column);

	column++;
	String houseNr = rs.getString(column);

	column++;
	String postalCode = rs.getString(column);

	column++;
	String city = rs.getString(column);

	column++;
	String countryCode = rs.getString(column);

	column++;
	double distance = rs.getDouble(column);

	if (countryCode != null && !countryCode.isBlank()) {
	    Country country = Environment.INSTANCE.getCountryMap().getCountry(countryCode);
	    Address address = new Address(city, postalCode, street, houseNr, country);
	    p.setAddress(address);
	    p.setDistanceFromAddress(new Distance(distance));
	}

	return p;
    }

    public void createPhoto(Photo p) throws SQLException, InterruptedException {
	SQLException exception = null;
	boolean success = false;
	for (int attempt = 0; success == false && attempt < MAX_RETRIES; attempt++) {
	    try {
		createPhotoAttempt(p);
		success = true;
		exception = null;
	    } catch (Exception e) {
		if (e instanceof SQLException) {
		    exception = (SQLException) e;
		} else {
		    exception = new SQLException("Cannot create photo", e);
		}
		success = false;
		try {
		    connection.rollback();
		} catch (SQLException sqlE) {
		    LOGGER.warn("Could not rollback because " + sqlE.getMessage());
		}
		Thread.sleep(SLEEP_BEFORE_RETRY_MILLISECONDS);

	    }
	}
	commit();
	if (success) {
	    // TimelineCache.getInstance().refresh();
	} else {
	    throw exception;
	}
    }

    private void createPhotoAttempt(Photo p) throws Exception {
	PreparedStatement st = null;
	ResultSet rsAlbum = null;

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("createPhotoAttempt " + p);
	}

	try {
	    st = getStatement(DBSQL.INSERT_PHOTOS);

	    st = getStatement(DBSQL.INSERT_PHOTOS);
	    st.clearParameters();
	    int column = 1;

//	    	1. bi.append(" id,");
	    st.setString(column, p.getId());
	    column++;

//		2. bi.append(" mediatype,");
	    String type = getPhotoType(p.getType());
	    st.setString(column, type);
	    column++;

//		4. bi.append(" dateTaken,");
	    LocalDateTime dateTaken = p.getDateTaken();

	    DBUtils.setTimestamp(st, column, dateTaken);
	    column++;

//		5. bi.append(" thumbWidth,");
	    st.setInt(column, p.getThumbWidth());
	    column++;

//		6. bi.append(" thumbHeight,");
	    st.setInt(column, p.getThumbHeight());
	    column++;

//		7. bi.append(" orientation,");
	    st.setInt(column, p.getOrientation());
	    column++;

//		8. bi.append(" latitude,");
	    if (p.getCoordinates() != null) {
		st.setDouble(column, p.getCoordinates().latitude());
		column++;
		st.setDouble(column, p.getCoordinates().longitude());
		column++;
	    } else {
		st.setNull(column, java.sql.Types.DOUBLE);
		column++;
		st.setNull(column, java.sql.Types.DOUBLE);
		column++;
	    }

//		10. bi.append(" duration,");
	    st.setInt(column, p.getDuration());
	    column++;

//		11. bi.append(" source,");
	    st.setString(column, p.getSource());
	    column++;

//		12. bi.append(" label,)");
	    st.setString(column, p.getLabel());
	    column++;

//		13. bi.append(" hidden)");
	    st.setBoolean(column, p.isHidden());
	    column++;

	    st.setBoolean(column, p.isLivePhoto());
	    column++;

	    Address address = p.getAddress();
	    if (address != null) {
		st.setString(column, address.getStreet());
		column++;

		st.setString(column, address.getNr());
		column++;

		st.setString(column, address.getPostalCode());
		column++;

		st.setString(column, address.getPlace());
		column++;

		st.setString(column, address.getCountry().code());
		column++;

		st.setDouble(column, p.getDistanceFromAddress().exact());
		column++;
	    } else {
		st.setNull(column, java.sql.Types.VARCHAR);
		column++;
		st.setNull(column, java.sql.Types.VARCHAR);
		column++;
		st.setNull(column, java.sql.Types.VARCHAR);
		column++;
		st.setNull(column, java.sql.Types.VARCHAR);
		column++;
		st.setNull(column, java.sql.Types.VARCHAR);
		column++;
		st.setNull(column, java.sql.Types.DOUBLE);
		column++;
		column = column + 6;
	    }

	    st.executeUpdate();
	} finally {
	    DBUtils.close(rsAlbum);
	}
    }

    private String getPhotoType(MediaType type) {
	String result;
	switch (type) {
	    case PHOTO:
		result = "p";
		break;
	    case VIDEO:
		result = "v";
		break;
	    default:
		throw new IllegalArgumentException("Unsoprted type " + type);
	}
	return result;
    }

    public List<Album> getAllAlbums(User user) throws SQLException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("getAllAlbums for " + user);
	}
	List<Album> albums = new ArrayList<>();
	String sql = DBSQL.GET_ALBUMS;
	// LOGGER.info(sql);
	PreparedStatement ps = getStatement(sql);
	ResultSet rs = null;
	try {
	    rs = ps.executeQuery();
	    while (rs.next()) {
		albums.add(parseAlbumResultSet(rs));
	    }
	} finally {
	    DBUtils.close(rs);
	}
	return albums;
    }

    public Album getAlbum(String relativeFolderName) throws SQLException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("getAlbum(relativeFolderName: " + relativeFolderName);
	}
	Album result = null;
	String sql = DBSQL.GET_ALBUM;

	PreparedStatement ps = getStatement(sql);
	ps.setString(1, relativeFolderName);

	ResultSet rs = null;
	try {
	    rs = ps.executeQuery();
	    if (rs.next()) {
		result = parseAlbumResultSet(rs);
	    }
	} finally {
	    DBUtils.close(rs);
	}
	return result;
    }

    public List<Album> getAlbumAndItsParents(String relativeFolderName) throws SQLException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("getAlbumHierarchy(relativeFolderName: " + relativeFolderName);
	}
	List<Album> result = new ArrayList<>();
	List<String> parts = StringUtils.split(relativeFolderName, '/');
	StringBuilder sb = new StringBuilder();
	String partPath = "";

	boolean first = true;
	for (int i = 0; i < parts.size(); i++) {
	    partPath = partPath + "/" + parts.get(i);
	    if (first) {
		first = false;
	    } else {
		sb.append(" / ");
	    }
	    Album child = getAlbum(partPath);
	    if (child != null) {
		result.add(child);
	    }
	}
	return result;
    }

    private Album parseAlbumResultSet(ResultSet rs) throws SQLException {
	Album result = new Album();
	result.setRelativeFolderName(rs.getString(1));
	result.setName(rs.getString(3));
//	4 b.append(" a.cover_photo_id,");
	String coverId = rs.getString(4);
//	5 b.append(" p.mediatype,");
	String type = rs.getString(5);
	if (type != null) {
	    MediaType mediaType;
	    if (type.equalsIgnoreCase("p")) {
		mediaType = MediaType.PHOTO;
	    } else {
		if (type.equalsIgnoreCase("v")) {
		    mediaType = MediaType.VIDEO;
		} else {
		    throw new IllegalStateException("Uknown type: " + type);
		}
	    }
//	6 b.append(" p.thumbwidth,");
	    int width = rs.getInt(6);
//	7 b.append(" p.thumbheight,");
	    int height = rs.getInt(7);
//	8 b.append(" p.title");
	    String title = rs.getString(8);
	    Photo cover = new Photo(coverId, mediaType);
	    cover.setThumbWidth(width);
	    cover.setThumbHeight(height);
	    cover.setLabel(title);
	    result.setCoverPhoto(cover);
	}
	return result;
    }

    public void addOrUpdateAlbum(User user, Album album) throws SQLException {
	restrictToAdmin(user);
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("addOrUpdate (user: " + user + ", album: " + album.toString());
	}
	Album existingAlbum = getAlbum(album.getRelativeFolderName());
	updateExistingAlbum(album, existingAlbum);
    }

    private void updateExistingAlbum(Album album, Album existingAlbum) throws SQLException {
	if (existingAlbum == null) {
	    createAlbum(album);
	} else {
	    PreparedStatement update = getStatement(DBSQL.UPDATE_ALBUM);
	    Role role = album.getRole();
	    if (role == null) {
		role = RoleModel.ROLE_USER; // existingAlbum.getRole();
	    }
	    String name = album.getName();
	    if (name == null) {
		name = existingAlbum.getName();
	    }
	    Photo cover = album.getCoverPhoto();
	    if (cover == null) {
		cover = existingAlbum.getCoverPhoto();
	    }
	    update.setString(1, role.name());
	    update.setString(2, name);
	    update.setString(3, cover.getId());
	    update.setString(4, album.getRelativeFolderName());
	    update.executeUpdate();
	}
    }

    private void createAlbum(Album album) throws SQLException {
	PreparedStatement psInsertAlbum = getStatement(DBSQL.INSERT_ALBUM);
	psInsertAlbum.setString(1, album.getRelativeFolderName());
	psInsertAlbum.setString(2, album.getRole().name());
	String name = album.getName();
	if (name == null || name.equals("")) {
	    name = StringUtils.findAfterLast(album.getRelativeFolderName(), "/");
	}
	DBUtils.setString(psInsertAlbum, 3, name);
	DBUtils.setString(psInsertAlbum, 4, album.getCoverPhoto().getId());
	psInsertAlbum.executeUpdate();

	File albumFolder = env.getFile(album.getRelativeFolderName());
	File parentFolder = albumFolder.getParentFile();
	String parentRelativeFolder = env.getRelativePhotoFileName(parentFolder);

	if (parentRelativeFolder != null && !parentRelativeFolder.equals("/")) {
	    Album parentAlbum = getAlbum(parentRelativeFolder);
	    if (parentAlbum == null) {
		parentAlbum = new Album();
		parentAlbum.setRelativeFolderName(parentRelativeFolder);
		parentAlbum.setName(parentFolder.getName());
		parentAlbum.setRole(album.getRole());

		Query query = new Query(RoleModel.getSystemUser());
		query.setRelativeFolderName(parentRelativeFolder);
		query.setIncludeSubFolders(true);
		query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
		List<Photo> allPhotos = query(query);
		if (allPhotos.size() > 0) {
		    Photo cover = allPhotos.get(0);
		    parentAlbum.setCoverPhoto(cover);
		}
		createAlbum(parentAlbum);
	    }
	}
    }

    public List<Photo> getPhotosIdsNotInAlbums(User user, int month, int year, OrderBy orderBy) throws SQLException {
	String order = "order by ";
	switch (orderBy) {
	    case DATE_TAKEN_NEWEST: {
		order = order + "p.datetaken desc";
		break;
	    }
	    case DATE_TAKEN_OLDEST: {
		order = order + "p.datetaken asc";
		break;
	    }
	    case FILENAME: {
		order = order + "f.filename";
		break;
	    }
	}
	List<Photo> result = new ArrayList<Photo>();
	ResultSet rs = null;
	try {
	    StringBuilder s = new StringBuilder();
	    s.append("select ");
	    s.append(DBSQL.GET_PHOTOS_ATTRIBUTES);
	    s.append(",f.relativefilename");
	    s.append(",f.relativefoldername");
	    s.append(",f.filename");
	    s.append(" from photos p inner join files f on p.id=f.photos_id ");
	    s.append("where f.relativefoldername LIKE ? ");
	    s.append("and extract (month from p.datetaken) = ? ");
	    s.append("and extract (year from p.datetaken) = ? ");
	    s.append("and p.id not in ");
	    s.append("( ");
	    s.append("	select p2.id ");
	    s.append("	from photos p2 inner join files f2 on p2.id=f2.photos_id ");
	    s.append("	and f2.relativefoldername LIKE ?  ");
	    s.append(") ");
	    s.append(order);
	    PreparedStatement st = getStatement(s.toString());
	    st.clearParameters();
	    st.setString(1, "/Uncategorized%");
	    st.setInt(2, month);
	    st.setInt(3, year);
	    st.setString(4, "/Albums%");

	    rs = st.executeQuery();
	    while (rs.next()) {
		Photo p = parsePhotoFilesSelect(rs, false);
		result.add(p);
	    }
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
	return result;
    }

    public SharedFile getSharedFile(String uuid) throws SQLException {
	String sql = "SELECT uuid, photos_id, expirationdate, creationdate, username FROM shares WHERE uuid=?";
	ResultSet rs = null;
	try {
	    PreparedStatement ps = getStatement(sql);
	    ps.setString(1, uuid);
	    rs = ps.executeQuery();
	    SharedFile sharedFile = null;
	    if (rs.next()) {
		LocalDateTime expirationDate = DBUtils.getDateTime(rs, 3);
		LocalDateTime creationDate = DBUtils.getDateTime(rs, 4);
		sharedFile = new SharedFile(rs.getString(1), rs.getString(2), expirationDate, creationDate, rs.getString(5));
	    }
	    return sharedFile;
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
    }

    public void addSharedFile(SharedFile sharedFile) throws SQLException {
	String sql = "INSERT INTO shares (uuid, photos_id, expirationdate, creationdate, username) VALUES (?,?,?,?,?);";
	PreparedStatement ps = getStatement(sql);
	ps.setString(1, sharedFile.uuid());
	ps.setString(2, sharedFile.photoId());
	DBUtils.setTimestamp(ps, 4, sharedFile.expirationDate());
	DBUtils.setTimestamp(ps, 3, sharedFile.creationDate());
	ps.setString(5, sharedFile.username());
	ps.executeUpdate();
    }

    public void setPreferences(String userName, Preferences preferences) throws SQLException {
	Preferences existing = getPreferences(userName);
	try {
	    String json = JSONUtilsFactory.getInstance().toJSON(preferences);
	    String sql;
	    PreparedStatement ps;
	    if (existing == null) {
		sql = "INSERT INTO preferences (username, preferences) VALUES (?, ?);";
		ps = getStatement(sql);
		ps.setString(1, userName);
		ps.setString(2, json);
	    } else {
		sql = "UPDATE preferences SET preferences = ? WHERE username = ?;";
		ps = getStatement(sql);
		ps.setString(2, userName);
		ps.setString(1, json);
	    }
	    ps.executeUpdate();
	} catch (Exception e) {
	    LOGGER.warn("Cannot set preferences " + preferences + ". Error: " + e.getMessage(), e);
	}
    }

    public Preferences getPreferences(String userName) throws SQLException {
	String sql = "SELECT preferences FROM preferences WHERE username = ?";
	ResultSet rs = null;
	try {
	    PreparedStatement ps = getStatement(sql);
	    ps.setString(1, userName);
	    rs = ps.executeQuery();
	    Preferences preferences = null;
	    if (rs.next()) {
		String json = rs.getString(1);
		try {
		    preferences = (Preferences) JSONUtilsFactory.getInstance().fromJSON(json, Preferences.class);
		} catch (Exception e) {
		    LOGGER.warn("Cannot read preferences JSON " + json + ". Error: " + e.getMessage());
		}
	    }
	    return preferences;
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
    }

    public void updateHiddenPhoto(String id, boolean hidden) throws SQLException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("updateHiddenPhoto('" + id + "', " + hidden + ")");
	}
	String sql = "UPDATE photos SET hidden = ? WHERE id = ?";
	PreparedStatement st = getStatement(sql);
	st.clearParameters();
	st.setBoolean(1, hidden);
	st.setString(2, id);
	int rows = st.executeUpdate();
	if (rows == 0) {
	    LOGGER.warn("No rows found to update hidden to " + hidden + " for id: " + id);
	} else {
	    if (rows != 1) {
		LOGGER.warn("Multiple (" + rows + ") rows updated with hidden for " + id);
	    }
	}
    }

    @Override
    public LocalDate getEarliestDayTaken(User user) throws SQLException {
	String sql = "SELECT MIN(datetaken) FROM photos";
	ResultSet rs = null;
	try {
	    PreparedStatement st = getStatement(sql);
	    rs = st.executeQuery();
	    LocalDate result;
	    if (rs.next()) {
		result = DBUtils.getDate(rs, 1);
	    } else {
		result = null;
	    }
	    return result;
	} finally {
	    DBUtils.close(rs);
	}
    }

    public MediaFile getMediaFile(String relativeFileName) throws SQLException {
	ResultSet rs = null;
	PreparedStatement st = getStatement(
		"SELECT relativeFileName, relativeFolderName, fileName, photos_id, role, dateModified, size, inalbums FROM Files WHERE relativeFileName=?");
	st.clearParameters();
	st.setString(1, relativeFileName);
	try {
	    MediaFile file = null;
	    rs = st.executeQuery();
	    if (rs.next()) {
		file = parseMediaFileResultSet(rs);
	    }
	    return file;
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
    }

    public List<MediaFile> getAllMediaFiles() throws SQLException {
	ResultSet rs = null;
	PreparedStatement st = getStatement("SELECT relativeFileName, relativeFolderName, fileName, photos_id, role, dateModified, size, inalbums FROM Files");
	st.clearParameters();
	try {
	    List<MediaFile> files = new ArrayList<MediaFile>();
	    rs = st.executeQuery();
	    while (rs.next()) {
		files.add(parseMediaFileResultSet(rs));
	    }
	    return files;
	} finally {
	    if (rs != null) {
		rs.close();
	    }
	}
    }

    private MediaFile parseMediaFileResultSet(ResultSet rs) throws SQLException {
	MediaFile file = null;
	file = new MediaFile();
	file.setRelativeFileName(rs.getString(1));
	file.setRelativeFolderName(rs.getString(2));
	file.setFileName(rs.getString(3));
	file.setId(rs.getString(4));
	file.setRole(RoleModel.getRole(rs.getString(5)));
	file.setFileLastModified(rs.getLong(6));
	file.setSize(rs.getLong(7));
	file.setInAlbums(rs.getBoolean(8));
	return file;
    }

    public void setMediaFile(MediaFile mediaFile) throws SQLException {
	ResultSet rsExists = null;
	ResultSet rsAlbum = null;
	try {
	    PreparedStatement stExists = getStatement("SELECT relativeFileName FROM files WHERE relativeFileName=?");
	    stExists.clearParameters();
	    stExists.setString(1, mediaFile.getRelativeFileName());
	    rsExists = stExists.executeQuery();
	    boolean exists = rsExists.next();

	    if (exists) {
		PreparedStatement stDelete = getStatement("DELETE FROM files WHERE relativeFileName = ?");
		stDelete.clearParameters();
		stDelete.setString(1, mediaFile.getRelativeFileName());
		int rows = stDelete.executeUpdate();
		if (rows != 1) {
		    LOGGER.warn("Deleting from files, 1 row expected but was " + rows);
		}
	    }
	    PreparedStatement st = getStatement(
		    "INSERT INTO files (relativeFileName, relativeFolderName, fileName, photos_id, role, dateModified, size, inalbums) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
	    st.clearParameters();
	    st.setString(1, mediaFile.getRelativeFileName());
	    st.setString(2, mediaFile.getRelativeFolderName());
	    st.setString(3, mediaFile.getFileName());
	    st.setString(4, mediaFile.getId());
	    st.setString(5, mediaFile.getRole().name());
	    st.setLong(6, mediaFile.getFileLastModified());
	    st.setLong(7, mediaFile.getSize());
	    st.setBoolean(8, mediaFile.isInAlbums());
	    st.executeUpdate();

	    ensureAlbumExists(mediaFile);

	} finally {
	    if (rsExists != null) {
		rsExists.close();
	    }
	    if (rsAlbum != null) {
		rsAlbum.close();
	    }
	}
    }

    private void ensureAlbumExists(MediaFile file) throws SQLException {
	String relativeFolderName = file.getRelativeFolderName();
	if (User.inUserAlbum(relativeFolderName)) {
	    Album album = getAlbum(relativeFolderName);
	    if (album == null) {
		album = new Album();
		album.setRelativeFolderName(relativeFolderName);
		String name = Environment.INSTANCE.getFile(relativeFolderName).getName();
		album.setName(name);
		album.setRole(file.getRole());
		Photo cover = new Photo();
		cover.setId(file.getId());
		album.setCoverPhoto(cover);
		LOGGER.info("Creating album: " + album);
		createAlbum(album);
	    }
	}
    }

    public void deleteMediaFile(MediaFile mediaFile) throws SQLException {
	String sql = "DELETE FROM files WHERE relativeFileName = ?";
	PreparedStatement st = getStatement(sql);
	st.setString(1, mediaFile.getRelativeFileName());
	int rows = st.executeUpdate();
	if (rows > 1) {
	    LOGGER.warn("Cannot remove mediafile, expected only one row but received " + rows);
	    rollback();
	}
    }

    public void updatePhoto(Photo p) throws SQLException {
	PreparedStatement st = getStatement(DBSQL.UPDATE_PHOTOS);
	st.clearParameters();

	int column = 1;
//	b.append(" mediatype = ?,");
	st.setString(column, getPhotoType(p.getType()));
	column++;

//	b.append(" dateTaken = ?,");

	LocalDateTime dateTaken = p.getDateTaken();
	DBUtils.setTimestamp(st, column, dateTaken);
	column++;

//	b.append(" thumbWidth = ?,");
	st.setInt(column, p.getThumbWidth());
	column++;

//	b.append(" thumbHeight = ?,");
	st.setInt(column, p.getThumbHeight());
	column++;

//	b.append(" orientation = ?,");
	st.setInt(column, p.getOrientation());
	column++;

//	b.append(" latitude = ?,");
	if (p.getCoordinates() != null) {
	    st.setDouble(column, p.getCoordinates().latitude());
	    column++;
	    st.setDouble(column, p.getCoordinates().longitude());
	    column++;
	} else {
	    st.setNull(column, java.sql.Types.DOUBLE);
	    column++;
	    st.setNull(column, java.sql.Types.DOUBLE);
	    column++;
	}

//	b.append(" duration = ?,");
	st.setInt(column, p.getDuration());
	column++;

//	b.append(" source = ?,");
	st.setString(column, p.getSource());
	column++;

//	b.append(" label = ?,");
	st.setString(column, p.getLabel());
	column++;

//	b.append(" hidden = ?");
	st.setBoolean(column, p.isHidden());
	column++;

	st.setBoolean(column, p.isLivePhoto());
	column++;

	Address address = p.getAddress();
	if (address != null) {
	    st.setString(column, address.getStreet());
	    column++;

	    st.setString(column, address.getNr());
	    column++;

	    st.setString(column, address.getPostalCode());
	    column++;

	    st.setString(column, address.getPlace());
	    column++;

	    st.setString(column, address.getCountry().code());
	    column++;

	    if (p.getDistanceFromAddress() != null) {
		st.setDouble(column, p.getDistanceFromAddress().exact());
	    } else {
		st.setNull(column, java.sql.Types.DOUBLE);
	    }
	    column++;
	} else {
	    st.setNull(column, java.sql.Types.VARCHAR);
	    column++;
	    st.setNull(column, java.sql.Types.VARCHAR);
	    column++;
	    st.setNull(column, java.sql.Types.VARCHAR);
	    column++;
	    st.setNull(column, java.sql.Types.VARCHAR);
	    column++;
	    st.setNull(column, java.sql.Types.VARCHAR);
	    column++;
	    st.setNull(column, java.sql.Types.DOUBLE);
	    column++;
	}

	st.setString(column, p.getId());

	int rows = st.executeUpdate();

	if (rows != 1) {
	    LOGGER.warn("Rolling back. Cannot update " + rows + " at once, can only update one row at the time");
	    rollback();
	}
    }

}
