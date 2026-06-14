#include "StandardRide.h"
#include "Ride.h"

StandardRide::StandardRide(std::string id, std::string pickup, std::string dropoff, double dist)
    : Ride(id, pickup, dropoff, dist) {}

double StandardRide::calculateFare() const {
    return 2.50 + (distance * 1.25);
}

void StandardRide::rideDetails() const {
    Ride::rideDetails();
    std::cout << " [Type: Standard, Fare: $" << calculateFare() << "]" << std::endl;
}