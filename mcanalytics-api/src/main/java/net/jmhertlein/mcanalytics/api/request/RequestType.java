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
package net.jmhertlein.mcanalytics.api.request;

/**
 *
 * @author joshua
 */
public enum RequestType {
    ONLINE_PLAYER_COUNT,
    PAST_ONLINE_PLAYER_COUNT,
    AUTHENTICATION,
    PASSWORD_RESET,
    NEW_PLAYER_LOGINS,
    UNIQUE_LOGINS_PER_DAY;

    @Override
    public String toString() {
        return name();
    }

}
