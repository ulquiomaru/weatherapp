package ulquiomaru.weatherapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

class GPSHelper {

    /*
     * Emulate GPS location:
     * - telnet localhost 5554
     * - geo fix <longitude value> <latitude value>
     */

    private LocationManager locationManager;
    private double latitude;
    private double longitude;

    GPSHelper(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    void getMyLocation() {
        List<String> providers = locationManager.getProviders(true);

        Location l = null;
        for (int i = 0; i < providers.size(); i++) {
            l = locationManager.getLastKnownLocation(providers.get(i));
            if (l != null)
                break;
        }
        if (l != null) {
            latitude = l.getLatitude();
            longitude = l.getLongitude();
        }
    }

    boolean isGPSenabled() {
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Function to get latitude
     */
    double getLatitude() {
        return latitude;
    }

    /**
     * Function to get longitude
     */
    double getLongitude() {
        return longitude;
    }
}
