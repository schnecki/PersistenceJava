package logic.information;

import persistence.*;

public class Country extends DBObject {

    private String name;
    private String code;

    public Country(String name) {
        this.name = name;
    }


    public Country(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Gets the value of code.
     *
     * @return the value of code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of code.
     *
     * @param argCode Value to assign to this.code.
     */
    public void setCode(String argCode) {
        this.code = argCode;
    }


    /**
     * Gets the value of name.
     *
     * @return the value of name.
     */
    public String getName() {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + ((code == null) ? "" : " (" + code + ")");
    }


    // Peristance information

    @Override
    protected DBEntry<String,Object>[] getTableColumns() {
        return new DBEntry[] {
            new DBEntry<String, Object>("country", name, true),
            new DBEntry<String, Object>("code", code)
        };
        // return columns;
    }

    @Override
    protected String getTableName() {
        return "country";
    }

    @Override
    protected void setAllFields(Object[] obj) {
        this.name = cast(obj[0]);
        this.code = cast(obj[1]);
    }


}
