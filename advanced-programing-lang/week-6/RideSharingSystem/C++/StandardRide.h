/**
 * @file StandardRide.h
 * @brief Concrete subclass for low-cost economy rides.
 */

#ifndef STANDARDRIDE_H
#define STANDARDRIDE_H

#include "Ride.h"

// DESIGN PRINCIPLE - INHERITANCE: StandardRide publicly inherits from Ride
class StandardRide : public Ride {
public:
    StandardRide(std::string id, std::string pickup, std::string dropoff, double dist);
    
    // DESIGN PRINCIPLE - POLYMORPHISM: Overrides virtual functions to enforce custom behavior
    double calculateFare() const override;
    void rideDetails() const override;
};

#endif