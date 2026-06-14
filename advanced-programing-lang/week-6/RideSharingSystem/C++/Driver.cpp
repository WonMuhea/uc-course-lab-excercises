#include "Driver.h"
#include "Ride.h" // Needed here to invoke actual methods inside implementation scope
#include <iostream>

Driver::Driver(std::string id, std::string name, double rating)
    : driverID(id), name(name), rating(rating) {}

void Driver::addRide(std::shared_ptr<Ride> ride) {
    assignedRides.push_back(ride);
}

void Driver::getDriverInfo() const {
    std::cout << "Driver: " << name << " (ID: " << driverID << ") | Rating: " << rating << "/5.0\n";
    std::cout << "Completed Workload Ledger:\n";
    if (assignedRides.empty()) {
        std::cout << "  - [No trips processed]\n";
        return;
    }
    double totalEarnings = 0;
    for (const auto& ride : assignedRides) {
        std::cout << "  - ";
        ride->rideDetails();
        totalEarnings += ride->calculateFare();
    }
    std::cout << "Total Driver Earnings: $" << totalEarnings << "\n";
}