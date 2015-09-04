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
public enum SQLString {
    CREATE_HOURLY_PLAYER_COUNT,
    ADD_HOURLY_PLAYER_COUNT,
    GET_HOURLY_PLAYER_COUNTS,
    CREATE_NEW_PLAYER_LOGIN,
    ADD_NEW_PLAYER_LOGIN,
    CREATE_PASSWORD_TABLE,
    GET_HASHSALT_FOR_USER,
    UPDATE_PLAYER_PASSWORD,
    ADD_NEW_USER,
    DELETE_USER,
    UPDATE_BOUNCED_PLAYER,
    GET_NEW_PLAYER_LOGINS_HOURLY,
    GET_UNIQUE_LOGINS,
    CREATE_PLAYER_LOGIN,
    ADD_LOGIN;
}
