#ifndef PREMIUMRIDE_H
#define PREMIUMRIDE_H

#include "Ride.h"

class PremiumRide : public Ride {
public:
    PremiumRide(std::string id, std::string pickup, std::string dropoff, double dist);
    double calculateFare() const override;
    void rideDetails() const override;
};

#endif