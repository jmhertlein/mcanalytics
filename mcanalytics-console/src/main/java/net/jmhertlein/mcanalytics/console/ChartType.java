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
package net.jmhertlein.mcanalytics.console;

/**
 *
 * @author joshua
 */
public enum ChartType {
    ONLINE_PLAYERS("Online Players"); //, NEW_PLAYER_LOGINS("First Logins"), BOUNCED_LOGINS("Bounced Logins");
    private final String desc;

    ChartType(String d) {
        desc = d;
    }

    @Override
    public String toString() {
        return desc;
    }

}
