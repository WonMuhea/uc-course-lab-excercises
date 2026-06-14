#ifndef RIDE_H
#define RIDE_H

#include <string>
#include <iostream>

class Ride {
protected:
    std::string rideID;
    std::string pickupLocation;
    std::string dropoffLocation;
    double distance;

public:
    Ride(std::string id, std::string pickup, std::string dropoff, double dist)
        : rideID(id), pickupLocation(pickup), dropoffLocation(dropoff), distance(dist) {}

    virtual ~Ride() = default;

    virtual double calculateFare() const = 0; 
    
    virtual void rideDetails() const {
        std::cout << "Ride [" << rideID << "] from " << pickupLocation 
                  << " to " << dropoffLocation << " (" << distance << " miles)";
    }

    double getDistance() const { return distance; }
    std::string getID() const { return rideID; }
};

#endif