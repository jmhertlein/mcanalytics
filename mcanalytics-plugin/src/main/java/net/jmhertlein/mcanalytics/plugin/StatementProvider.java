/*
 * Copyright (C) 2015 Joshua Michael Hertlein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mcanalytics.plugin;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to load SQL statements from a package on the classpath.
 *
 * Normally, the "root" is the path "/db" In this path is a series of SQL files (.sql). Ignoring the
 * extension, there should be one file for each enum value in SQLString with the same name as the
 * enum.
 *
 * Ex:
 *
 * Suppose SQLString.SELECT_QUERY and SQLString.CREATE_STMT exist. There should be two files, like
 * so: /db/SELECT_QUERY.sql /db/CREATE_STMT.sql
 *
 * If there is a DBMS-specific query available, then it should be in a sub-folder under /db with the
 * same name as is returned by SQLBackend.YOUR_BACKEND_HERE.toString(). The StatementProvider will
 * try to load each query from the DBMS-specific folder first, and fall back to the generic folder
 * if it cannot find a DBMS-specific query.
 *
 * Ex.
 *
 * For most backends, SELECT_QUERY.sql is the same. But for postgres, there is a very useful
 * optimization we can do. So we should create /db/postgres/SELECT_QUERY.sql Then, if we are using
 * postgres, the generic query will be ignored and the postgres one will be used instead.
 *
 * @author joshua
 * @see SQLString
 * @see SQLBackend
 */
public class StatementProvider {
    private final Map<SQLString, String> stmts;

    public StatementProvider(String root, SQLBackend db) {
        stmts = new HashMap<>();
        String[] path = new String[]{root + "/" + db.toString(), root};

        valueLoop:
        for(SQLString sqlString : SQLString.values()) {
            InputStream src = null;
            for(String loc : path) {
                String resourcePath = loc + "/" + sqlString + ".sql";
                src = getClass().getResourceAsStream(resourcePath);
                if(src != null) {
                    break;
                }
            }

            if(src == null)
                System.err.println("Unable to find statement \"" + sqlString.name() + "\" for backend type \"" + db.toString() + "\".");
            else
                stmts.put(sqlString, cat(src));
        }
    }

    public String get(SQLString s) {
        return stmts.get(s);
    }

    private static String cat(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
