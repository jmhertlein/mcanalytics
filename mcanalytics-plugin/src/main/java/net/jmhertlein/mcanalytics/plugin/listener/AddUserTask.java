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
package net.jmhertlein.mcanalytics.plugin.listener;

import java.sql.Connection;
import java.sql.SQLException;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;

/**
 *
 * @author joshua
 */
public class AddUserTask extends WriteTask {
    private String username, password;

    public AddUserTask(String username, String password, MCAnalyticsPlugin p) {
        super(p);
        this.username = username;
        this.password = password;
    }

    @Override
    public void gather() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        byte[] salt = SSLUtil.newSalt();
        throw new UnsupportedOperationException();
        // TODO: finish implementing this- still need the SQL file + enum
    }

}
