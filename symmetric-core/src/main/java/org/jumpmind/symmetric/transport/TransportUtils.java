/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.symmetric.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TransportUtils {
    public static BufferedReader toReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public static BufferedWriter toWriter(OutputStream os) {
        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    public static String toCSV(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        for (Object key : map.keySet()) {
            buff.append(key).append(":").append(map.get(key)).append(",");
        }
        buff.setLength(buff.length() - 1);
        return buff.toString();
    }
}