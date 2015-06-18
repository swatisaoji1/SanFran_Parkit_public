package parking.group6.csc413.projectmap;

/**
 * This object is used to hold information when adding a favorite place to park, which is then
 * added to the Favorites table in the database.
 * @author Darin Evanow
 * @version 1
 */
public class Parking {

    private String address;
    private Double latitude;
    private Double longitude;
    private String times[] = {""};
    private boolean isFavorite = false;

    /**
     * Default constructor
     */
    public Parking() {

    }

    /**
     * Constructor
     * @param address
     * @param latitude
     * @param longitude
     * @param times
     */
    public Parking(String address, double latitude, double longitude, String[] times) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.times = times;
    }

    /**
     * Sets the address of the Parking object
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the address of the Parking object
     * @return String address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Sets the latitude of the parking object
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     * Sets the Latitude and longitude of the parking object
     * @param location
     */
    public void setLatLong(String location) {
        String[] parts = location.split(",");
        this.latitude = Double.parseDouble(parts[0]);
        this.longitude = Double.parseDouble(parts[1]);
    }

    /**
     * Returns the Latitude of the Parking object
     * @return double
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Sets the Longitude of the Parking object
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the Longitude of the parking object.
     * @return double
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Set the time of the Parking Object
     * @param times
     */
    public void setTimes(String[] times) {
        this.times = times;
    }

    /**
     * Returns the time of the Parking Object
     * @return String[]
     */
    public String[] getTimes() {
        return this.times;
    }

    /**
     * Returns the time as formatted string .
     * @return String
     */
    public String getTimesAsString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < times.length; i++) {
            sb.append(times[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
       return "Parking: " + this.getAddress();
    }

    public boolean equals(Object obj){
        if (!(obj instanceof Parking))
            return false;
        if (obj == this)
            return true;

        Parking rhs = (Parking) obj;
        if (rhs.getAddress().equals(this.getAddress())){
            return true;
        }else{
            return false;
        }

    }
}
