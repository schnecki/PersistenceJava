package persistence;

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import exceptions.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DBObject {

    private static final String FK = "_FK_";

    private boolean toUpdate = false;

    /**
     * Define the table columns for this object.
     *
     * @return the value of tableColumns.
     */
    protected abstract DBEntry<String,Object>[] getTableColumns();

    /**
     * Define the table name for this object.
     *
     * @return the value of getTableName.
     */
    protected abstract String getTableName();


    /** Order as given in getTableColumns. */
    protected abstract void setAllFields(Object[] data);


    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x) {
        return (T) x;
    }


    public boolean save()
        throws DBException {
        if (getTableName() == null || getTableName().length() == 0)
            throw new DBObjectDefinitionException("Invalid Table name: " +
                                                  getTableName());

        boolean result = true;
        StringBuilder str0 = null;
        StringBuilder str1 = new StringBuilder();

        if (toUpdate) {
            str0 = new StringBuilder("update " + getTableName() + " set ");
        } else {
            str0 = new StringBuilder("insert into " + getTableName() + " ");
            str1 = new StringBuilder(" values ");
        }

        DBEntry<String, Object>[] elems = this.getTableColumns();


        for (int i = 0; i < elems.length; i++) {

            DBEntry<String,Object> e = elems[i];

            if (i == 0 && !toUpdate) {
                str0.append('(');
                str1.append('(');
            }

            if (e.value != null && DBObject.class.isAssignableFrom(e.value.getClass())) {
                // another DBObject, update and reference
                result &= ((DBObject) e.value).save();

                // get FK
                DBEntry <String, Object>[] fk = getPrimaryKeys((DBObject) e.value);
                for (int j = 0; j < fk.length; j++) {
                    DBEntry<String, Object> e2 = fk[j];

                    // add foreign keys
                    if (toUpdate) {
                        str0.append(e.column);
                        str0.append(FK);
                        str0.append(e2.column);
                        str0.append('=');
                        if (e2.value == null) {
                            str0.append("NULL");
                        } else {
                            str0.append('\'');
                            str0.append(e2.value);
                            str0.append('\'');
                        }
                    } else {
                        // to be inserted
                        str0.append(e.column);
                        str0.append(FK);
                        str0.append(e2.column);
                        if (e2.value == null) {
                            str1.append("NULL");
                        } else {
                            str1.append('\'');
                            str1.append(e2.value);
                            str1.append('\'');
                        }

                    }

                    if (j+1 < fk.length) {
                        str0.append(", ");
                        str1.append(", ");
                    }
                }

            } else {

                // normal value to insert or update
                if (toUpdate) {
                    str0.append(e.column);
                    str0.append('=');
                    if (e.value == null) {
                        str0.append("NULL");
                    } else {
                        str0.append('\'');
                        str0.append(e.value);
                        str0.append('\'');
                    }
                } else {
                    str0.append(e.column);
                    if (e.value == null) {
                        str1.append("NULL");
                    } else {
                        str1.append('\'');
                        str1.append(e.value);
                        str1.append('\'');
                    }
                }

            }

            // add comma or parenthesis
            if (i+1==elems.length) {
                if (!toUpdate) {
                    str0.append(')');
                    str1.append(')');
                    str0.append(str1);
                } else {
                    str0.append(" where ");
                    str0.append(getWhereClause(this));
                }
            } else {
                str0.append(", ");
                str1.append(", ");

            }


        }

        if (Constants.DEBUG)
            System.out.println("DEBUG SQL: " + str0);


        // TODO save to db
        return DBConnection.update(str0.toString());
    }


    /** The values get immediatelly set. You do NOT need to cast the
     *  Object back to it's previous type and save it again. Just call
     *  object.load() to load the values from the database in the
     *  object.
     */
    public DBObject load()
        throws DBException {

        StringBuilder str = new StringBuilder("select ");
        DBEntry<String, Object>[] elems = getTableColumns();
        Object[] data = new Object[elems.length];


        // iterate over columns
        int j = 0;
        for (int i = 0; i < elems.length; i++) {
            DBEntry<String, Object> e = elems[i];

            if (e.value != null && DBObject.class.isAssignableFrom(e.value.getClass())) {
                DBObject chld = (DBObject)e.value;
                data[i] = chld.load();
                str.append(e.column + FK);
                str.append(getPrimaryKeys(chld)[j++].column);
            } else {
                str.append(e.column);
            }
            if (i + 1 < elems.length)
                str.append(", ");
        }

        // from
        str.append(" from ");
        str.append(getTableName());
        str.append(" where ");
        StringBuilder whereClause = getWhereClause(this);
        if (whereClause == null) {
            whereClause = getAlternativeWhereClause(this);
        }
        if (whereClause == null) {
            throw new DBException("Could not determine where clause in SQL statement!");
        }


        str.append(whereClause);


        if (Constants.DEBUG)
            System.out.println("DEBUG SQL: " + str);


        // get Data
        Object[] rs = DBConnection.selectFirst(str.toString());

        if (rs == null)
            throw new DBException("Result was null. SQL:" + str.toString());

        System.out.println("select result = " + rs);

        // iterate over columns

        for (int k = 0, i = 0; i < elems.length; k++, i++) {
            DBEntry<String, Object> e = elems[i];

            // skip included DBObject(s)
            if (e.value != null &&
                DBObject.class.isAssignableFrom(e.value.getClass())) {
                continue;
            }
            data[i] = rs[k];
        }


        // get data TODO     ///////////////////////////////////////////////////////////
        // data[0] = "asdf";

        // load into fields
        this.setAllFields(data);

        this.toUpdate = true;

        return this;
    }


    private StringBuilder getAlternativeWhereClause(DBObject object) {
        return getAlternativeWhereClause(object, "");
    }

    private StringBuilder getAlternativeWhereClause(DBObject object,
                                                    String toAppendBeforeColumnName) {
        StringBuilder str = new StringBuilder();
        boolean start = true;
        DBEntry<String, Object>[] cols = object.getTableColumns();

        for (int i = 0; i < cols.length; i++) {
            DBEntry<String, Object> e = cols[i];

            if (e.value == null || e.value.toString().length() == 0)
                continue;

            if (!start)
                str.append(" AND ");
            else
                start = false;

            if (DBObject.class.isAssignableFrom(e.value.getClass())) {
                // get FK
                str.append(getAlternativeWhereClause((DBObject) e.value, e.column + FK));
            } else {
                str.append(toAppendBeforeColumnName);
                str.append(e.column);
                str.append("=");
                str.append('\'');
                str.append(e.value);
                str.append('\'');
            }

            if (i+1 != cols.length)
                str.append(" AND ");

        }
        return str;

    }

    private StringBuilder getWhereClause(DBObject object)
        throws DBException {
        return getWhereClause(object, "");
    }

    private StringBuilder getWhereClause(DBObject object, String toAppendBeforeColumnName)
        throws DBException {

        StringBuilder str = new StringBuilder();
        DBEntry<String, Object>[] pks;

        try {
            pks = getPrimaryKeys(object);
        } catch (DBObjectPrimaryKeyException e) {
            return null;
        }

        for (int i = 0; i < pks.length; i++) {
            DBEntry<String, Object> e = pks[i];

           if (e.value != null && DBObject.class.isAssignableFrom(e.value.getClass())) {
               // get FK
               str.append(getWhereClause((DBObject) e.value, e.column + FK));
           } else {
               str.append(toAppendBeforeColumnName);
               str.append(e.column);
               str.append("=");
               str.append('\'');
               str.append(e.value);
               str.append('\'');


           }

           if (i+1 != pks.length)
               str.append(" AND ");

        }
        return str;
    }


    private static DBEntry<String, Object>[] getPrimaryKeys(DBObject object)
        throws DBException {

        List<DBEntry<String, Object>> list = new ArrayList<DBEntry<String, Object>>();

        for (DBEntry<String,Object> e : object.getTableColumns()) {
                if (e.isPrimary) {
                    if (e.value == null || e.value.toString().length() == 0)
                        throw new DBObjectPrimaryKeyException("Primary key cannot be null. " +
                                                              "Object info: " + e);
                list.add(e);

            }
        }

        // if no primary key is set, use all fields as pk's
        if (list.size() == 0) {
            for (DBEntry<String,Object> e : object.getTableColumns()) {
                list.add(e);
            }
        }

        if (list.size() == 0) {
            throw new DBObjectDefinitionException("Object without definition of columns. " +
                                                  "Define getTableColumns() for the object!");
        }


        // convert to array
        DBEntry<String, Object>[] array = new DBEntry[list.size()];
        int i = 0;
        for (DBEntry<String, Object> e : list) {
            array[i] = list.get(i);
            i++;
        }


        return array;
    }


}
