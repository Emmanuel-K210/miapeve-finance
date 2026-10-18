package com.miapeve.finance.ui;

import java.sql.SQLException;

public interface CompteChangeListener {
    void onComptesChanged() throws SQLException;
}