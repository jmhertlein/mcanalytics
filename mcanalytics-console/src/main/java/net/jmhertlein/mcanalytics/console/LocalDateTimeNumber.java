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

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author joshua
 */
public class LocalDateTimeNumber extends Number {
    private final LocalDateTime value;

    public LocalDateTimeNumber(LocalDateTime value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return Long.valueOf(value.atZone(ZoneId.systemDefault()).toEpochSecond()).intValue();
    }

    @Override
    public long longValue() {
        return value.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @Override
    public float floatValue() {
        return value.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @Override
    public double doubleValue() {
        return value.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
