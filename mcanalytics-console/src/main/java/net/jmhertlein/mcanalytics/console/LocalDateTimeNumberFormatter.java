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
import java.time.ZoneOffset;
import javafx.util.StringConverter;

/**
 *
 * @author joshua
 */
public class LocalDateTimeNumberFormatter extends StringConverter<Number> {
    private final ZoneOffset offset;

    public LocalDateTimeNumberFormatter() {
        offset = LocalDateTime.now().atZone(ZoneId.systemDefault()).getOffset(); //is this seriously the only way to do this?!!
    }

    @Override
    public String toString(Number object) {
        return LocalDateTime.ofEpochSecond(object.longValue(), 0, offset).toString();
    }

    @Override
    public Number fromString(String string) {
        return new LocalDateTimeNumber(LocalDateTime.parse(string));
    }

}
