package com.fleettracking.app.util;

/**
 * Fleet-wide configuration constants.
 *
 * The depot (home base / yard) is where vehicles physically live when they are
 * not on a mission. A newly added vehicle starts here, and an idle vehicle keeps
 * its last known position — which, for a brand-new car, is the depot.
 */
public final class FleetConfig {

    private FleetConfig() {}

    /** Depot location (central Casablanca). Single depot for now. */
    public static final double DEPOT_LAT = 33.5731;
    public static final double DEPOT_LNG = -7.5898;
}
