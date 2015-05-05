package parking.group6.csc413.projectmap;

/**
 * This object is used to hold information when adding a favorite place to park, which is then
 * added to the Favorites table in the database.
 */
public class Parking {

    private String address;
    private Double latitude;
    private Double longitude;
    private String times[];
    private boolean isFavorite = false;

    public Parking() {

    }

    public Parking(String address, double latitude, double longitude, String[] times) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.times = times;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    

    public void setLatLong(String location) {
        String[] parts = location.split(",");
        this.latitude = Double.parseDouble(parts[0]);
        this.longitude = Double.parseDouble(parts[1]);
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }

    public String[] getTimes() {
        return this.times;
    }

    public String getTimesAsString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < times.length; i++) {
            sb.append(times[i]);
            sb.append("\n");
        }
        return sb.toString();
    }
}
