/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2022 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreAggregate
 */
public class PostgreAggregate extends PostgreProcedure {

    private long oid;
    private final PostgreSchema schema;
    private String name;
    private final boolean persisted;

    public PostgreAggregate(DBRProgressMonitor monitor, PostgreSchema schema, ResultSet dbResult)
        throws SQLException, DBException {
        super(schema);
        this.schema = schema;
        loadAggregateInfo(monitor, dbResult);
        this.loadInfo(monitor, dbResult);
        persisted = true;

    }

    private void loadAggregateInfo(DBRProgressMonitor monitor, ResultSet dbResult)
        throws SQLException, DBException {
        this.oid = JDBCUtils.safeGetLong(dbResult, "proc_oid");
        this.name = JDBCUtils.safeGetString(dbResult, "proc_name");
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return name;
    }

    @Property(viewable = true, order = 2)
    public List<PostgreDataType> getInputTypes(DBRProgressMonitor monitor) throws DBException {
        List<PostgreDataType> result = new ArrayList<>();
        for (PostgreProcedureParameter param : getInputParameters()) {
            result.add(param.getParameterType());
        }
        return result;
    }

    @Override
    public boolean supportsObjectDefinitionOption(String option) {
        return super.supportsObjectDefinitionOption(option);
    }

    @Override
    public DBSProcedureType getProcedureType() {
        return DBSProcedureType.FUNCTION;
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().getAggregateCache().refreshObject(monitor, getContainer(), this);
    }

    @Property(viewable = true, order = 3)
    public PostgreDataType getOutputType(DBRProgressMonitor monitor) throws DBException {
        return getReturnType();
    }

    @Property(viewable = false, order = 80)
    @Override
    public long getObjectId() {
        return oid;
    }

    @Override
    public DBSObject getParentObject() {
        return schema;
    }

    @NotNull
    @Override
    public PostgreDataSource getDataSource() {
        return schema.getDataSource();
    }

    @NotNull
    @Override
    public PostgreDatabase getDatabase() {
        return schema.getDatabase();
    }

    @Override
    public String getDescription() {
        return null;
    }



    @Override
    public void setObjectDefinitionText(String sourceText) {
        super.setObjectDefinitionText(sourceText);
    }

//    private loadAdditionalInfo() {
//
//    }

    @Override
    protected void createBody(DBRProgressMonitor monitor) throws DBException {
        setBody("TO-BE DONE");

    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @NotNull
    @Override
    public String getOverloadedName() {
        return PostgreProcedure.makeOverloadedName(schema, name, getInputParameters(), true, false);
    }

}

