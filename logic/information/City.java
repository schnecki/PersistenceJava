package logic.information;

import persistence.*;

public class City extends DBObject {

    private String name;
    private Country country;


    public City(String name, Country country) {
        if (name == null || country == null)
            throw new IllegalArgumentException();
        this.name = name;
        this.country = country;
    }


    /**
     * Gets the value of country.
     *
     * @return the value of country.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the value of country.
     *
     * @param argCountry Value to assign to this.country.
     */
    public void setCountry(Country argCountry) {
        this.country = argCountry;
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
     * Sets the value of name.
     *
     * @param argName Value to assign to this.name.
     */
    public void setName(String argName) {
        this.name = argName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + " (" + country + ")";
    }


    // Persistance information

    @Override
    protected DBEntry<String,Object>[] getTableColumns() {
        return new DBEntry[] {
            new DBEntry<String, Object>("city", name, true),
            new DBEntry<String, Object>("country", country, true)
        };
    }

    @Override
    protected String getTableName() {
        return "city";
    }

    @Override
    protected void setAllFields(Object[] obj) {
        this.name = cast(obj[0]);
        this.country = cast(obj[1]);
    }

}
