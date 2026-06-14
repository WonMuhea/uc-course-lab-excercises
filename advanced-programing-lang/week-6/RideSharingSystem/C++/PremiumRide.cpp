#include "PremiumRide.h"
#include "Ride.h"

PremiumRide::PremiumRide(std::string id, std::string pickup, std::string dropoff, double dist)
    : Ride(id, pickup, dropoff, dist) {}

double PremiumRide::calculateFare() const {
    return 5.00 + (distance * 2.50);
}

void PremiumRide::rideDetails() const {
    Ride::rideDetails();
    std::cout << " [Type: PREMIUM ⭐, Fare: $" << calculateFare() << "]" << std::endl;
}