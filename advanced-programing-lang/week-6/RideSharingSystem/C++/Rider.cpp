#include "Rider.h"
#include "Ride.h" // Needed here to invoke actual methods inside implementation scope
#include <iostream>

Rider::Rider(std::string id, std::string name) : riderID(id), name(name) {}

void Rider::requestRide(std::shared_ptr<Ride> ride) {
    requestedRides.push_back(ride);
}

void Rider::viewRides() const {
    std::cout << "Rider History for " << name << " (ID: " << riderID << "):\n";
    if (requestedRides.empty()) {
        std::cout << "  - [No ride history recorded]\n";
        return;
    }
    for (const auto& ride : requestedRides) {
        std::cout << "  - ";
        ride->rideDetails(); 
    }
}