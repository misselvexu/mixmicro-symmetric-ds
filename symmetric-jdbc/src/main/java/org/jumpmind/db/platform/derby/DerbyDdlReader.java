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
package org.jumpmind.db.platform.derby;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.ForeignKey;
import org.jumpmind.db.model.IIndex;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.model.Trigger;
import org.jumpmind.db.model.TypeMap;
import org.jumpmind.db.model.Trigger.TriggerType;
import org.jumpmind.db.platform.AbstractJdbcDdlReader;
import org.jumpmind.db.platform.DatabaseMetaDataWrapper;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.db.sql.ISqlRowMapper;
import org.jumpmind.db.sql.JdbcSqlTemplate;
import org.jumpmind.db.sql.Row;
import org.jumpmind.db.sql.SqlException;

/*
 * Reads a database model from a Derby database.
 */
public class DerbyDdlReader extends AbstractJdbcDdlReader {
    public DerbyDdlReader(IDatabasePlatform platform) {
        super(platform);
    }

    @Override
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
        Column column = super.readColumn(metaData, values);
        String defaultValue = column.getDefaultValue();
        if (defaultValue != null) {
            // we check for these strings
            // GENERATED_BY_DEFAULT -> 'GENERATED BY DEFAULT AS IDENTITY'
            // AUTOINCREMENT: start 1 increment 1 -> 'GENERATED ALWAYS AS
            // IDENTITY'
            if ("GENERATED_BY_DEFAULT".equals(defaultValue)
                    || defaultValue.startsWith("AUTOINCREMENT:")) {
                column.setDefaultValue(null);
                column.setAutoIncrement(true);
            } else if (TypeMap.isTextType(column.getMappedTypeCode())) {
                column.setDefaultValue(unescape(defaultValue, "'", "''"));
            }
        }
        return column;
    }

    @Override
    protected boolean isInternalForeignKeyIndex(Connection connection,
            DatabaseMetaDataWrapper metaData, Table table, ForeignKey fk, IIndex index) {
        return isInternalIndex(index);
    }

    @Override
    protected boolean isInternalPrimaryKeyIndex(Connection connection,
            DatabaseMetaDataWrapper metaData, Table table, IIndex index) {
        return isInternalIndex(index);
    }

    /*
     * Determines whether the index is an internal index, i.e. one created by Derby.
     * 
     * @param index The index to check
     * 
     * @return <code>true</code> if the index seems to be an internal one
     */
    private boolean isInternalIndex(IIndex index) {
        String name = index.getName();
        // Internal names normally have the form "SQL051228005030780"
        if ((name != null) && name.startsWith("SQL")) {
            try {
                Long.parseLong(name.substring(3));
                return true;
            } catch (NumberFormatException ex) {
                // we ignore it
            }
        }
        return false;
    }

    @Override
    public List<Trigger> getTriggers(final String catalog, final String schema,
            final String tableName) throws SqlException {
        List<Trigger> triggers = new ArrayList<Trigger>();
        log.debug("Reading triggers for: " + tableName);
        JdbcSqlTemplate sqlTemplate = (JdbcSqlTemplate) platform
                .getSqlTemplate();
        String sql = "SELECT "
                + "TRIG.TRIGGERNAME as TRIGGER_NAME, "
                + "TAB.TABLENAME as TABLE_NAME, "
                + "SC.SCHEMANAME as SCHEMA_NAME, "
                + "TRIG.CREATIONTIMESTAMP, "
                + "TRIG.EVENT as TRIGGER_TYPE, "
                + "TRIG.STATE, "
                + "TRIG.FIRINGTIME as TRIGGER_TIME, "
                + "TRIG.WHENSTMTID, "
                + "TRIG.ACTIONSTMTID, "
                + "TRIG.REFERENCEDCOLUMNS, "
                + "TRIG.TRIGGERDEFINITION as source, "
                + "TRIG.REFERENCINGOLD, "
                + "TRIG.REFERENCINGNEW, "
                + "TRIG.OLDREFERENCINGNAME, "
                + "TRIG.NEWREFERENCINGNAME, "
                + "TRIG.TYPE, "
                + "TRIG.TRIGGERID, "
                + "TRIG.TABLEID, "
                + "TRIG.SCHEMAID "
                + "FROM SYS.SYSTRIGGERS AS TRIG "
                + "INNER JOIN SYS.SYSTABLES AS TAB "
                + "ON TAB.TABLEID = TRIG.TABLEID "
                + "INNER JOIN SYS.SYSSCHEMAS AS SC "
                + "ON SC.SCHEMAID = TRIG.SCHEMAID "
                + "WHERE TABLENAME=? and SC.SCHEMANAME=? ";
        triggers = sqlTemplate.query(sql, new ISqlRowMapper<Trigger>() {
            public Trigger mapRow(Row row) {
                Trigger trigger = new Trigger();
                trigger.setName(row.getString("TRIGGER_NAME"));
                trigger.setSchemaName(row.getString("SCHEMA_NAME"));
                trigger.setTableName(row.getString("TABLE_NAME"));
                if (row.getString("STATE").equals("E")) {
                    row.put("STATE", "ENABLED");
                    trigger.setEnabled(true);
                } else if (row.getString("STATE").equals("D")) {
                    row.put("STATE", "DISABLED");
                    trigger.setEnabled(false);
                }
                String event = row.getString("TRIGGER_TYPE");
                switch (event.charAt(0)) {
                    case ('I'):
                        event = "INSERT";
                        break;
                    case ('D'):
                        event = "DELETE";
                        break;
                    case ('U'):
                        event = "UPDATE";
                }
                row.put("TRIGGER_TYPE", event);
                trigger.setTriggerType(TriggerType.valueOf(event));
                if (row.getString("TRIGGER_TIME").equals("A"))
                    row.put("TRIGGER_TIME", "AFTER");
                else if (row.getString("TRIGGER_TIME").equals("B"))
                    row.put("TRIGGER_TIME", "BEFORE");
                trigger.setMetaData(row);
                trigger.setSource(row.getString("source"));
                row.remove("source");
                return trigger;
            }
        }, tableName, schema);
        return triggers;
    }
}
