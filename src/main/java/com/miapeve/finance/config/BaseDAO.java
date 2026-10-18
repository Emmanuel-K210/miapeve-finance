package com.miapeve.finance.config;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        DatabaseManager.getInstance();
		return DatabaseManager.getConnection();
    }
}