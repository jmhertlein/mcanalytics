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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.mcanalytics.api.auth.SSLUtil;
import net.jmhertlein.mcanalytics.plugin.MCAnalyticsPlugin;
import net.jmhertlein.mcanalytics.plugin.SQLString;
import net.jmhertlein.mcanalytics.plugin.StatementProvider;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author joshua
 */
public class AddUserTask extends WriteTask {
    private final String username, password;

    public AddUserTask(String username, String password, MCAnalyticsPlugin p) {
        super(p);
        this.username = username;
        this.password = password;
    }

    @Override
    protected void write(Connection c, StatementProvider stmts) throws SQLException {
        byte[] salt = SSLUtil.newSalt();
        byte[] pass;
        try {
            pass = password.getBytes("UTF-8");
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(AddUserTask.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        byte[] hash;
        try {
            hash = SSLUtil.hash(pass, salt);
        } catch(NoSuchAlgorithmException | NoSuchProviderException ex) {
            Logger.getLogger(AddUserTask.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try(PreparedStatement addUser = c.prepareStatement(stmts.get(SQLString.ADD_NEW_USER))) {
            addUser.setString(1, username);
            addUser.setString(2, Base64.encodeBase64String(hash));
            addUser.setString(3, Base64.encodeBase64String(salt));
            addUser.executeUpdate();
        }
    }

}
