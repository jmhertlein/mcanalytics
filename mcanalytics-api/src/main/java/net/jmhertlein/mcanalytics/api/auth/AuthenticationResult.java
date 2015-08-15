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
package net.jmhertlein.mcanalytics.api.auth;

import java.security.cert.X509Certificate;

/**
 *
 * @author joshua
 */
public class AuthenticationResult {
    private final X509Certificate cert, ca;
    private final Boolean success;

    public AuthenticationResult(X509Certificate cert, X509Certificate ca, Boolean success) {
        this.cert = cert;
        this.ca = ca;
        this.success = success;
    }

    public boolean hasCertificate() {
        return cert != null;
    }

    public boolean hasCA() {
        return ca != null;
    }

    public X509Certificate getCA() {
        return ca;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public Boolean getSuccess() {
        return success;
    }

}
