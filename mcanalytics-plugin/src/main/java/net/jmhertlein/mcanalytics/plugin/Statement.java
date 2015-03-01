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

/**
 *
 * @author joshua
 */
public enum Statement {
    CREATE_HOURLY_PLAYER_COUNT("CREATE TABLE IF NOT EXISTS HourlyPlayerCount(instant DATETIME, count INTEGER);"),
    ADD_HOURLY_PLAYER_COUNT("INSERT INTO HourlyPlayerCount VALUES(?, ?);");

    private final String sql;

    Statement(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }

}
